package com.suporter.android.data.model

import com.google.gson.annotations.SerializedName

data class ProjectsResponse(
    @SerializedName("projects") val projects: List<ProjectDto> = emptyList(),
    @SerializedName("count") val count: Int = 0
)

data class ProjectDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("uuid") val uuid: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("event_type") val eventType: String? = null,
    @SerializedName("html_template") val htmlTemplate: String? = null,
    @SerializedName("css_style") val cssStyle: String? = null,
    @SerializedName("duration") val duration: Int? = null
)
