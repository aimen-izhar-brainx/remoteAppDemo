package com.example.remoteappdemo


/*
class CastDiscoveryProvider(context: Context?) : DiscoveryProvider {
    private val mMediaRouter: MediaRouter?
    private var mMediaRouteSelector: MediaRouteSelector? = null
    protected var mMediaRouterCallback: MediaRouter.Callback
    private val removedUUID: MutableList<String> = CopyOnWriteArrayList()
    protected var foundServices: ConcurrentHashMap<String, ServiceDescription?>
    protected var serviceListeners: CopyOnWriteArrayList<DiscoveryProviderListener>
    private var removeRoutesTimer: Timer? = null
    var isRunning = false

    init {
        mMediaRouter = createMediaRouter(context)
        mMediaRouterCallback = MediaRouterCallback()
        foundServices = ConcurrentHashMap<String, ServiceDescription?>(8, 0.75f, 2)
        serviceListeners = CopyOnWriteArrayList<DiscoveryProviderListener>()
    }

    protected fun createMediaRouter(context: Context?): MediaRouter {
        return MediaRouter.getInstance(context!!)
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        if (mMediaRouteSelector == null) {
            mMediaRouteSelector = try {
                MediaRouteSelector.Builder()
                    .addControlCategory(
                        CastMediaControlIntent.categoryForCast(
                            CastService.getApplicationID()
                        )
                    )
                    .build()
            } catch (e: IllegalArgumentException) {
                Log.w(Util.T, "Invalid application ID: " + CastService.getApplicationID())
                for (listener in serviceListeners) {
                    listener.onServiceDiscoveryFailed(
                        this, ServiceCommandError(
                            0,
                            "Invalid application ID: " + CastService.getApplicationID(), null
                        )
                    )
                }
                return
            }
        }
        rescan()
    }

    fun stop() {
        isRunning = false
        if (removeRoutesTimer != null) {
            removeRoutesTimer!!.cancel()
            removeRoutesTimer = null
        }
        if (mMediaRouter != null) {
            Util.runOnUI(Runnable { mMediaRouter.removeCallback(mMediaRouterCallback) })
        }
    }

    fun restart() {
        stop()
        start()
    }

    fun reset() {
        stop()
        foundServices.clear()
    }

    fun rescan() {
        Util.runOnUI(Runnable {
            mMediaRouter!!.addCallback(
                mMediaRouteSelector!!, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY
            )
        })
    }

    fun addListener(listener: DiscoveryProviderListener) {
        serviceListeners.add(listener)
    }

    fun removeListener(listener: DiscoveryProviderListener) {
        serviceListeners.remove(listener)
    }

    fun addDeviceFilter(filter: DiscoveryFilter?) {}
    fun removeDeviceFilter(filter: DiscoveryFilter?) {}
    fun setFilters(filters: List<DiscoveryFilter?>?) {}
    val isEmpty: Boolean
        get() = false

    private inner class MediaRouterCallback : MediaRouter.Callback() {
        override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) {
            super.onRouteAdded(router, route)
            val castDevice = CastDevice.getFromBundle(route.extras)
            val uuid = castDevice!!.deviceId
            removedUUID.remove(uuid)
            var foundService: ServiceDescription? = foundServices[uuid]
            val isNew = foundService == null
            var listUpdateFlag = false
            if (isNew) {
                foundService = ServiceDescription(
                    CastService.ID, uuid,
                    castDevice.ipAddress!!.hostAddress
                )
                foundService.setFriendlyName(castDevice.friendlyName)
                foundService.setModelName(castDevice.modelName)
                foundService.setModelNumber(castDevice.deviceVersion)
                foundService.setModelDescription(route.description)
                foundService.setPort(castDevice.servicePort)
                foundService.setServiceID(CastService.ID)
                foundService.setDevice(castDevice)
                listUpdateFlag = true
            } else {
                if (!foundService.getFriendlyName().equals(castDevice.friendlyName)) {
                    foundService.setFriendlyName(castDevice.friendlyName)
                    listUpdateFlag = true
                }
                foundService.setDevice(castDevice)
            }
            foundService.setLastDetection(Date().time)
            foundServices[uuid] = foundService
            if (listUpdateFlag) {
                for (listenter in serviceListeners) {
                    listenter.onServiceAdded(this@CastDiscoveryProvider, foundService)
                }
            }
        }

        override fun onRouteChanged(router: MediaRouter, route: MediaRouter.RouteInfo) {
            super.onRouteChanged(router, route)
            val castDevice = CastDevice.getFromBundle(route.extras)
            val uuid = castDevice!!.deviceId
            val foundService: ServiceDescription? = foundServices[uuid]
            val isNew = foundService == null
            var listUpdateFlag = false
            if (!isNew) {
                foundService.setIpAddress(castDevice.ipAddress!!.hostAddress)
                foundService.setModelName(castDevice.modelName)
                foundService.setModelNumber(castDevice.deviceVersion)
                foundService.setModelDescription(route.description)
                foundService.setPort(castDevice.servicePort)
                foundService.setDevice(castDevice)
                if (!foundService.getFriendlyName().equals(castDevice.friendlyName)) {
                    foundService.setFriendlyName(castDevice.friendlyName)
                    listUpdateFlag = true
                }
                foundService.setLastDetection(Date().time)
                foundServices[uuid] = foundService
                if (listUpdateFlag) {
                    for (listenter in serviceListeners) {
                        listenter.onServiceAdded(this@CastDiscoveryProvider, foundService)
                    }
                }
            }
        }

        override fun onRoutePresentationDisplayChanged(
            router: MediaRouter,
            route: MediaRouter.RouteInfo
        ) {
            Log.d(
                Util.T, "onRoutePresentationDisplayChanged: [" + route.name + "] ["
                        + route.description + "]"
            )
            super.onRoutePresentationDisplayChanged(router, route)
        }

        override fun onRouteRemoved(router: MediaRouter, route: MediaRouter.RouteInfo) {
            super.onRouteRemoved(router, route)
            val castDevice = CastDevice.getFromBundle(route.extras)
            val uuid = castDevice!!.deviceId
            removedUUID.add(uuid)

            // Prevent immediate removing. There are some cases when service is removed and added
            // again after a second.
            if (removeRoutesTimer == null) {
                removeRoutesTimer = Timer()
                removeRoutesTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        removeServices(route)
                    }
                }, ROUTE_REMOVE_INTERVAL)
            }
        }

        override fun onRouteVolumeChanged(router: MediaRouter, route: MediaRouter.RouteInfo) {
            Log.d(
                Util.T, "onRouteVolumeChanged: [" + route.name + "] ["
                        + route.description + "]"
            )
            super.onRouteVolumeChanged(router, route)
        }

        private fun removeServices(route: MediaRouter.RouteInfo) {
            for (uuid in removedUUID) {
                val service: ServiceDescription? = foundServices[uuid]
                if (service != null) {
                    Log.d(Util.T, "Service [" + route.name + "] has been removed")
                    Util.runOnUI(Runnable {
                        for (listener in serviceListeners) {
                            listener.onServiceRemoved(this@CastDiscoveryProvider, service)
                        }
                    })
                    foundServices.remove(uuid)
                }
            }
            removedUUID.clear()
        }
    }

    companion object {
        private const val ROUTE_REMOVE_INTERVAL: Long = 3000
    }
}
*/

