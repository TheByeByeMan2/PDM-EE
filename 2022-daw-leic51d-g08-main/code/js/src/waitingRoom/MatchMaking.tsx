import * as React from "react"
import { useEffect, useState } from "react"
import { UserIdAndToken } from "../login/login";
import { getUserComplextInfo, URL_ALL_RULES } from "../requests";
import { Rule } from "../rule/Rule";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { buildRuleInfo, buildRuleOptions, exempleRule } from "../rule/RuleInfo";
import { waitingSender } from "./waitingRoomSender";
import { useCurrentLocalData, useSetCurrentLocalData } from "../localData/LocalData";
import { UserState, WaitingRoomMsgObj, WaitingRoomResponse } from "./waittingRoomDomains";
import { printlnLog } from "../utils";

enum WaitingState {
    VERIFIRE = 'VERIFIRE', INIT = 'INIT', SEARCH = 'SEARCH', FINISH = 'FISHI'
}

function matchMakingLog(msg) { printlnLog('MATCHMAKING', msg) }

export function MatchMaking() {
    matchMakingLog('entry')
    const localData = useCurrentLocalData()
    const setLocaData = useSetCurrentLocalData()
    const [notification, setNotification] = useState('Select Game Rule')
    const [allRules, setAllRules] = useState(undefined)
    const [state, setState] = useState({ rule: "" });
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [error, setError] = useState(undefined)
    const rule = React.useRef<HTMLSelectElement>(null);
    const formValue = (event: React.ChangeEvent<HTMLSelectElement>) => {
        setState({ ...state, [event.target.name]: event.target.value.trim() });
    };
    const [firstFlag, setFirstFlag] = useState(true)
    const location = useLocation()
    const [redirect, setRedirect] = useState(false)
    const [lastChatMsg, setLastChatMsg] = useState(undefined)

    useEffect(() => {
        matchMakingLog('creating new Event Source')
        const eventSource = new EventSource("/api/games/waiting/chat")
        eventSource.onmessage = (ev) => {
            setLastChatMsg(ev.data)
            return () => {
                matchMakingLog('close event')
                eventSource.close()
            }
        }
    }, [])

    // verifiry and Auto reconnect
    // fix the detect time
    if (state) {
        useEffect(() => {
            matchMakingLog('Verifiry the reconnection')
            getUserComplextInfo(localData.userIdAndToken).then((complextUser) => {
                if (complextUser.userState === UserState.BATTLE) {
                    try {
                        setError(undefined)
                        if (complextUser.gameId !== null && complextUser.ruleId !== null) {
                            matchMakingLog('Get the user info and reconnect')
                            setLocaData({ userIdAndToken: localData.userIdAndToken, game: { gameId: complextUser.gameId, ruleId: complextUser.ruleId } })
                            setRedirect(true)
                        } else {
                            throw new Error('The user State is Battle but not has game Info, PANIC ')
                        }
                    } catch (e) {
                        setError(e)
                    }
                }
            })
        }, [])
    }

    // nao é ativado

    // redirect when has other player with same rule
    useEffect(() => {
        matchMakingLog('Start Manager chat mensage')
        if (lastChatMsg !== undefined && isSubmitting) {
            matchMakingLog('get player msg')
            const obj = JSON.parse(lastChatMsg)
            const msg:WaitingRoomMsgObj = JSON.parse(obj.msg)
            matchMakingLog('parsed obj: '+JSON.stringify(obj) +' and rule is '+rule.current.value)
            matchMakingLog('my info ' + JSON.stringify(localData))
            if (msg.userId !== localData.userIdAndToken.userId && msg.ruleId === Number(rule.current.value)) {
                matchMakingLog('catch other player msg: '+JSON.stringify(obj))
                getMyWaiting().then((value) => {
                    if (value.gameId !== null) {
                        matchMakingLog('found other player and redirect')
                        console.log(value)
                        setLocaData({ userIdAndToken: localData.userIdAndToken, game: { gameId: value.gameId, ruleId: value.ruleId } })
                        setRedirect(true)
                    }
                })
            } else {matchMakingLog('catch msg faild')}
        }
    }, [lastChatMsg])


    useEffect(() => {
        if (allRules === undefined) {
            getAllGameRule(localData.userIdAndToken).then(res => {
                matchMakingLog('Get Game Rule')
                setAllRules(res)
            })
        }
    }, [])


    window.onpopstate = (event) => {
        if (isSubmitting) {
            matchMakingLog('exit waiting room')
            exitWaitingRoom()
        }
        window.onpopstate = () => { }
    }


    async function getMyWaiting(): Promise<WaitingRoomResponse | undefined> {
        try {
            const resp = await fetch("api/games/waiting", {
                method: "GET",
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': localData.userIdAndToken.token,
                    'userId': localData.userIdAndToken.userId.toString()
                },
            })
            if (resp.status != 200) {
                setError(`error, unexpected status ${resp.status}`)
            }
            return await resp.json()
        } catch (err) {
            setIsSubmitting(false)
            setError(err.message)
        }
    }

    async function exitWaitingRoom() {
        try {
            const resp = await fetch("api/games/waiting/exit", {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': localData.userIdAndToken.token
                },
                body: JSON.stringify({
                    'userId': localData.userIdAndToken.userId,
                }),
            })
            if (resp.status != 200) {
                setError(`error, unexpected status ${resp.status}`)
            }
        } catch (err) {
            setIsSubmitting(false)
            setError(err.message)
        }
    }

    async function handleSubmit(ev) {
        ev.preventDefault()
        setIsSubmitting(true)
        matchMakingLog('submit and get waiting room')
        setNotification('Wating other player with same rule')
        matchMakingLog('selected rule = ' + rule.current.value)
        try {
            const ruleId = rule.current.value
            const resp = await fetch("api/games/waiting", {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': localData.userIdAndToken.token
                },
                body: JSON.stringify({
                    'userId': localData.userIdAndToken.userId,
                    'ruleId': ruleId
                }),
            }
            )
            const respBody: WaitingRoomResponse = await resp.json()
            if (resp.status != 200) {
                setError(`error, unexpected status ${resp.status} and ${JSON.stringify(respBody)}`)
            }

            if (respBody.gameId !== null) {
                setLocaData({userIdAndToken: localData.userIdAndToken, game: {gameId: respBody.gameId, ruleId: respBody.ruleId}})
                setRedirect(true)
            }
            const msg: WaitingRoomMsgObj = { userId: localData.userIdAndToken.userId, ruleId: respBody.ruleId }
            await waitingSender(JSON.stringify(msg))
        } catch (err) {
            setIsSubmitting(false)
            setError(err)
        }
    }

    const ruleInfoPath = function (ruleId) {
        if (ruleId == null) return 'rule/info' + 1
        return 'rule/' + ruleId.value
    }
    if (redirect) {
        return <Navigate to={"/game"} state={{ source: location.pathname }} replace={true} />
    } else return (
        <><h1>Rules</h1>
            <table>
                <tbody>
                    <tr>
                        <td>
                            <form onSubmit={handleSubmit}>
                                <fieldset disabled={isSubmitting}>
                                    {buildRuleOptions(allRules, rule, formValue, 'rule', state.rule)}
                                    <button type="submit">Start</button>
                                    <div hidden={isSubmitting}><Link to={'rule/add'}> Add New Rule </Link></div>
                                </fieldset>
                            </form>
                        </td>
                        <td>
                            <div>Rule Information</div>
                            <fieldset>
                                {buildRuleInfo(getRuleById(allRules, rule.current))}
                            </fieldset>
                        </td>
                    </tr>
                    <tr>
                        <div>{notification}</div>
                    </tr>
                    <tr>
                        <div>{error}</div>
                    </tr>

                </tbody>
            </table></>
    );
}

async function getAllGameRule(userIdAndToken: UserIdAndToken): Promise<Array<Rule> | undefined> {
    async function doFetch() {
        const resp = await fetch(URL_ALL_RULES, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': userIdAndToken.token,
                'userId': userIdAndToken.userId.toString()
            }
        })
        const bodyRes: Array<Rule> = await resp.json()
        if (bodyRes) {
            return bodyRes
        } else return undefined
    }
    return await doFetch()
}

function getRuleById(allRules: Rule[], currentRule) {
    if (!allRules) return undefined
    if (currentRule == null) return allRules[0]
    else return allRules[currentRule.value - 1]
}