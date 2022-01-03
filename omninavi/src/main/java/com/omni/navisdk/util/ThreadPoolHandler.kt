package com.omni.navisdk.util

import android.app.Activity
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ThreadPoolHandler {
    companion object {
        private const val dTag = "ThreadPoolHandler"
        private const val corePoolSize = 3
        private const val maxPoolSize = corePoolSize + 3
        private const val keepAliveTime = 60L
        private val timeUnit = TimeUnit.SECONDS
        private lateinit var workQueue: LinkedBlockingQueue<Runnable>
        private lateinit var miscThread: ArrayList<HandlerThread>
        private lateinit var miscHandler: ArrayList<Handler>
        private var mainThreadHandler: Handler? = null
        private const val miscThreadCnt = 3
        private var nextThread = 0
        var locationThreadHandler: Handler? = null
        private lateinit var miscThreadHandler: ArrayList<Handler>
        private lateinit var executor: ThreadPoolExecutor
        private var init = false
        fun runOnMain(r: () -> Unit) {
            if(mainThreadHandler == null) mainThreadHandler = Handler(Looper.getMainLooper())
            mainThreadHandler?.post(r)
        }
        fun runOnMain(r: () -> Unit, delay: Int) {
            if(mainThreadHandler == null) mainThreadHandler = Handler(Looper.getMainLooper())
            mainThreadHandler?.postDelayed(r, delay.toLong())
        }
        fun runOnMain(r: () -> Unit, delay: Long) {
            if(mainThreadHandler == null) mainThreadHandler = Handler(Looper.getMainLooper())
            mainThreadHandler?.postDelayed(r, delay)
        }
        fun runOnMain(r: Runnable, delay: Int) {
            if(mainThreadHandler == null) mainThreadHandler = Handler(Looper.getMainLooper())
            mainThreadHandler?.postDelayed(r, delay.toLong())
        }
        fun runOnMain(r: Runnable, delay: Long) {
            if(mainThreadHandler == null) mainThreadHandler = Handler(Looper.getMainLooper())
            mainThreadHandler?.postDelayed(r, delay)
        }
        fun cancelWorkOnMain(r: () -> Unit) = mainThreadHandler?.removeCallbacks(r)

        /*        fun runOnWorker(r: () -> Unit) = if (!executor.isTerminated) executor.execute(r) else {
                    LogUtil.d(dTag, "worker executor is isTerminated!, using signal thread")
                    Thread(r).start()
                }*/
        fun runOnWorker(r: () -> Unit) {
            var init = ::miscHandler.isInitialized
            if (init) init = miscHandler[nextThread].sendEmptyMessage(1)
            if (init) {
                miscHandler[nextThread].post(r)
                nextThread = (nextThread + 1) % miscThreadCnt
            } else {
                createHandlers()
                Thread(r).start()
            }
        }

        fun runOnWorker(r: () -> Unit, delay: Int) {
            var init = ::miscHandler.isInitialized
            if (init) init = miscHandler[nextThread].sendEmptyMessage(1)
            if (init) {
                miscHandler[nextThread].postDelayed(r, delay.toLong())
                nextThread = (nextThread + 1) % miscThreadCnt
            } else {
                createHandlers()
                Handler(Looper.getMainLooper()).postDelayed({
                    Thread(r).start()
                }, delay.toLong())
            }
        }

        fun runOnWorker(r: Runnable, delay: Int, index: Int) {
            var init = ::miscHandler.isInitialized
            if (init) init = miscHandler[nextThread].sendEmptyMessage(1)
            if (init) {
                miscHandler[index].postDelayed(r, delay.toLong())
            } else {
                createHandlers()
                Handler(Looper.getMainLooper()).postDelayed({
                    Thread(r).start()
                }, delay.toLong())
            }
        }


        fun runOnWorker(r: () -> Unit, delay: Int, index: Int) {
            var init = ::miscHandler.isInitialized
            if (init) init = miscHandler[nextThread].sendEmptyMessage(1)
            if (init) {
                miscHandler[index].postDelayed(r, delay.toLong())
            } else {
                createHandlers()
                Handler(Looper.getMainLooper()).postDelayed({
                    Thread(r).start()
                }, delay.toLong())
            }
        }

        fun runOnWorker(r: Runnable, delay: Int) {
            var init = ::miscHandler.isInitialized
            if (init) init = miscHandler[nextThread].sendEmptyMessage(1)
            if (init) {
                miscHandler[nextThread].postDelayed(r, delay.toLong())
                nextThread = (nextThread + 1) % miscThreadCnt
            } else {
                createHandlers()
                Handler(Looper.getMainLooper()).postDelayed({
                    Thread(r).start()
                }, delay.toLong())
            }
        }

        fun runOnLocation(r: () -> Unit) {
            if (locationThreadHandler == null)
                HandlerThread("location_${System.currentTimeMillis()}", Thread.MIN_PRIORITY).apply {
                    start()
                    locationThreadHandler = Handler(looper)
                }
            locationThreadHandler?.apply {
                if (looper.thread.isAlive) post(r)
            }
        }

        fun runOnLocation(r: () -> Unit, delay: Int) {
            if (locationThreadHandler == null)
                HandlerThread("location_${System.currentTimeMillis()}", Thread.MIN_PRIORITY).apply {
                    start()
                    locationThreadHandler = Handler(looper)
                }
            locationThreadHandler?.apply {
                if (looper.thread.isAlive) postDelayed(r, delay.toLong())
            }
        }

        fun cancelWorkOnWorker(r: () -> Unit) = executor.remove(r)
        fun cancelWorkerTask(r: Runnable, index: Int) =
            miscThreadHandler.get(index).removeCallbacks(r)

        fun cancelAll() {
            workQueue.apply {
                for (r in iterator()) executor.remove(r)
            }
        }

        fun init(activity: FragmentActivity) {
            createHandlers()
            activity.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_CREATE -> {
                        }
                        Lifecycle.Event.ON_START -> {
                            if (!init) createHandlers()
                        }
                        Lifecycle.Event.ON_RESUME -> {

                        }
                        Lifecycle.Event.ON_PAUSE -> {
                            //destroy()
                        }
                        Lifecycle.Event.ON_STOP -> {
                            destroy()
                        }
                        Lifecycle.Event.ON_DESTROY -> {

                        }
                        Lifecycle.Event.ON_ANY -> {

                        }
                    }
                }
            })
        }

        private fun createHandlers() {
            init = true

            val miscThread =  ArrayList<HandlerThread> ()
            val miscHandler =  ArrayList<Handler> ()
            for (i in 0 until corePoolSize) {
                miscThread.add(HandlerThread("${dTag}_HandlerThread_${i}_${System.currentTimeMillis()}").apply { start() })
                miscHandler.add(Handler(miscThread[i].looper))
            }
            this.miscThread = miscThread
            this.miscHandler = miscHandler
            workQueue = LinkedBlockingQueue()
            executor =
                ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, timeUnit, workQueue)
            mainThreadHandler = Handler(Looper.getMainLooper())
            miscThreadHandler = arrayListOf()
            for (i in 0 until miscThreadCnt)
                HandlerThread("miscThread_${System.currentTimeMillis()}").apply {
                    start()
                    miscThreadHandler.add(Handler(looper))
                }
            HandlerThread("location_${System.currentTimeMillis()}", Thread.MIN_PRIORITY).apply {
                start()
                locationThreadHandler = Handler(looper)
            }

        }

        fun destroy(force: Boolean = false) {
            init = false
            for (i in 0 until corePoolSize) {
                miscThread[i].quitSafely()
            }
            executor.apply {
                if (force) shutdownNow() else shutdown()
            }
            for (m in miscThreadHandler.iterator()) {
                m.looper.quitSafely()
            }
            mainThreadHandler = null
            locationThreadHandler?.apply {
                looper.quitSafely()
            }
        }
    }
}