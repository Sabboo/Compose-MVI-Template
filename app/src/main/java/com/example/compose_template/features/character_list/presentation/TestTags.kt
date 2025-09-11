package com.example.compose_template.features.character_list.presentation

object TestTags {
    const val SCREEN_NAME = "character_list_screen_"
    const val LOADING_INDICATOR = "${SCREEN_NAME}screen_loading_indicator"
    const val ERROR_WIDGET = "${SCREEN_NAME}error_state_widget"
    const val ERROR_WIDGET_TEXT = "${SCREEN_NAME}error_state_text"
    const val EMPTY_WIDGET = "${SCREEN_NAME}empty_state_widget"
    const val EMPTY_WIDGET_TEXT = "${SCREEN_NAME}empty_state_text"
    const val CHARACTERS_LIST = "${SCREEN_NAME}characters_list"
    fun characterListItem(id: Int) = "${SCREEN_NAME}characterItem_${id}"
}
