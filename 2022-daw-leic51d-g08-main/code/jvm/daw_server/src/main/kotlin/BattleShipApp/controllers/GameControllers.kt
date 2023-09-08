package BattleShipApp.controllers

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PreDestroy
import kotlin.concurrent.withLock


@RestController
class GameControllers(
    private val chatService: ChatService
) {

    @GetMapping("/api/games/{id}/chat")
    fun gameListener(@PathVariable id: String): SseEmitter {
        val listener = chatService.newListenerInGame()
        return listener.sseEmitter
    }

    @PostMapping("/api/games/{id}/chat")
    fun gameSend(@RequestBody message: String, @PathVariable id: String) {
        chatService.sendMessageToGame(message)
    }

    @GetMapping("/api/games/waiting/chat")
    fun gameListener(): SseEmitter {
        val listener = chatService.newListenerInWaitingRoom()
        return listener.sseEmitter
    }

    @PostMapping("/api/games/waiting/chat")
    fun gameSend(@RequestBody message: String) {
        chatService.sendMessageToWaiting(message)
    }

}

@Service
class ChatService {

    private val waitingRoom = mutableListOf<ChatListener>()
    private val game = mutableListOf<ChatListener>()
    private var currentGameListenerId = 0L
    private var currentWaitingListenerId = 0L
    private val lock = ReentrantLock()

    val gameScheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1).also {
        it.scheduleAtFixedRate({ keepGameAlive() }, 2, 2, TimeUnit.SECONDS)
    }

    val waitingScheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1).also {
        it.scheduleAtFixedRate({ keepWaitingAlive() }, 2, 2, TimeUnit.SECONDS)
    }

    enum class ListenersType {
        WAITING_ROOM, GAME
    }

    private fun addNewListener(type: ListenersType, gameId: Int? = null): ChatListener = lock.withLock {
        logger.info("newListener with ${type.name} type")
        val sseEmitter = SseEmitter(TimeUnit.MINUTES.toMillis(5))
        val listener = ChatListener(sseEmitter)
        when (type) {
            ListenersType.WAITING_ROOM -> {
                waitingRoom.add(listener)
                listener.sseEmitter.onCompletion {
                    removeWaitingListener(listener)
                }
            }
            ListenersType.GAME -> {
                game.add(listener)
                listener.sseEmitter.onCompletion {
                    removeGameListener(listener, gameId!!)
                }
            }
        }
        listener
    }

    fun newListenerInWaitingRoom(): ChatListener = addNewListener(ListenersType.WAITING_ROOM)

    @PreDestroy
    private fun preDestroy() {
        logger.info("shutting down All")
        gameScheduler.shutdown()
        waitingScheduler.shutdown()
    }

    fun newListenerInGame(): ChatListener = addNewListener(ListenersType.GAME)

    private fun sendMessage(type: ListenersType, msg: String) = lock.withLock {
        logger.info("sendMessage with ${type.name} type and msg = $msg")
        when (type) {
            ListenersType.WAITING_ROOM -> {
                val id = currentWaitingListenerId++
                sendWaitingRoomEventToAll(Event.Message(id, msg))
            }
            ListenersType.GAME -> {
                val id = currentGameListenerId++
                sendGameEventToAll(Event.Message(id, msg))
            }
        }
    }

    fun sendMessageToGame(msg: String) = sendMessage(ListenersType.GAME, msg)
    fun sendMessageToWaiting(msg: String) = sendMessage(ListenersType.WAITING_ROOM, msg)

    private fun removeGameListener(listener: ChatListener, gameId: Int) = removeListener(ListenersType.GAME, listener)
    private fun removeWaitingListener(listener: ChatListener) = removeListener(ListenersType.WAITING_ROOM, listener)

    private fun removeListener(type:ListenersType,listener: ChatListener) = lock.withLock {
        logger.info("remove ${type.name}")
        when (type){
            ListenersType.WAITING_ROOM -> {
                waitingRoom.remove(listener)
            }
            ListenersType.GAME -> {
                game.remove(listener)
            }
        }

    }

    private fun keepGameAlive() = keepAlive(ListenersType.GAME)
    private fun keepWaitingAlive() = keepAlive(ListenersType.WAITING_ROOM)

    private fun keepAlive(type: ListenersType) = lock.withLock {
        val keepAlive = Event.KeepAlive(Instant.now().epochSecond)
        when (type){
            ListenersType.WAITING_ROOM -> {
                sendWaitingRoomEventToAll(keepAlive)
            }
            ListenersType.GAME -> {
                sendGameEventToAll(Event.KeepAlive(Instant.now().epochSecond))
            }
        }
    }

    private fun sendGameEventToAll(event: Event) {
        game.forEach {
            try {
                event.writeTo(it.sseEmitter)
            } catch (ex: Exception) {
                logger.info("Exception while sending event - {}", ex.message)
            }
        }
    }

    private fun sendWaitingRoomEventToAll(event: Event) {
        waitingRoom.forEach {
            try {
                event.writeTo(it.sseEmitter)
            } catch (ex: Exception) {
                logger.info("Exception while sending event - {}", ex.message)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ChatService::class.java)
    }

}

sealed interface Event {

    fun writeTo(emitter: SseEmitter)

    class Message(val id: Long, val msg: String) : Event {

        private val event = SseEmitter.event()
            .id(id.toString())
            .name("message")
            .data(this)

        override fun writeTo(emitter: SseEmitter) {
            emitter.send(
                event
            )
        }
    }

    class KeepAlive(val timestamp: Long) : Event {

        private val event = SseEmitter.event()
            .comment(timestamp.toString())

        override fun writeTo(emitter: SseEmitter) {
            emitter.send(
                event
            )
        }
    }
}

class ChatListener(
    val sseEmitter: SseEmitter
)