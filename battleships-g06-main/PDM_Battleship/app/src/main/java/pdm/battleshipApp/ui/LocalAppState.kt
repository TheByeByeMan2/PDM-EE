package pdm.battleshipApp.ui

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize


data class LocalAppState(
    var username: String? = null,
    var isLogin: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeByte(if (isLogin) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocalAppState> {
        override fun createFromParcel(parcel: Parcel): LocalAppState {
            return LocalAppState(parcel)
        }

        override fun newArray(size: Int): Array<LocalAppState?> {
            return arrayOfNulls(size)
        }
    }
}
