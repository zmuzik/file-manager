package zmuzik.filemanager.common.bus

import android.os.Handler
import android.os.Looper
import com.squareup.otto.Bus

class UiBus : Bus() {

    private val handler = Handler(Looper.getMainLooper())

    override fun post(event: Any) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event)
        } else {
            handler.post { super@UiBus.post(event) }
        }
    }

    override fun register(consumerObj: Any) {
        try {
            super.register(consumerObj)
        } catch (t: Throwable) {
            // intentionally left blank
            // prevent exception when called by repeatedly run lifecycle method
        }
    }

    override fun unregister(consumerObj: Any) {
        try {
            super.unregister(consumerObj)
        } catch (t: Throwable) {
            // intentionally left blank
            // prevent exception when called by repeatedly run lifecycle method
        }
    }

    companion object {

        private val bus: UiBus = UiBus()

        fun get(): UiBus {
            return bus
        }
    }
}
