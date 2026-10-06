package com.abrarshakhi.lumen.core.domain.platform

enum class HomeWidget { Search, Notes }

enum class TileRequestResult { Added, AlreadyAdded, NotAdded, Unsupported }

interface HomeScreenSurfaces {
    val canPinWidgets: Boolean

    val canRequestTile: Boolean

    fun pinWidget(widget: HomeWidget): Boolean

    suspend fun requestTile(): TileRequestResult
}
