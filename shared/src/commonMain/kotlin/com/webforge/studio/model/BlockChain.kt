package com.webforge.studio.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class BlockChain(
    val id: String,
    val elementId: String? = null,
    val eventType: BlockEventType,
    val blocks: List<BlockNode> = emptyList(),
)

@Serializable
data class BlockNode(
    val id: String,
    val chainId: String,
    val type: BlockType,
    val order: Int,
    val parameters: Map<String, JsonPrimitive> = emptyMap(),
    val connectedChainId: String? = null,
    val parentBlockId: String? = null,
    val isDisabled: Boolean = false,
)

@Serializable
enum class BlockEventType {
    ON_CLICK,
    ON_LONG_PRESS,
    ON_HOVER_ENTER,
    ON_HOVER_LEAVE,
    ON_SCROLL_INTO_VIEW,
    ON_PAGE_LOAD,
    ON_INPUT_CHANGE,
    ON_FORM_SUBMIT,
    ON_KEY_PRESS,
    ON_DOUBLE_CLICK,
}

@Serializable
enum class BlockType {
    // Event blocks
    ON_CLICK,
    ON_LONG_PRESS,
    ON_HOVER_ENTER,
    ON_HOVER_LEAVE,
    ON_SCROLL_INTO_VIEW,
    ON_PAGE_LOAD,
    ON_INPUT_CHANGE,
    ON_FORM_SUBMIT,
    ON_KEY_PRESS,
    ON_DOUBLE_CLICK,

    // Logic blocks
    IF_CONDITION,
    ELSE_BRANCH,
    ELSE_IF_BRANCH,
    FOR_LOOP,
    WHILE_LOOP,
    SWITCH_CASE,
    CASE_BRANCH,
    TRY_BLOCK,
    CATCH_BLOCK,
    BREAK,
    CONTINUE,
    RETURN,

    // Dom manipulation blocks
    GET_ELEMENT_BY_ID,
    GET_ELEMENT_BY_CLASS,
    SET_TEXT_CONTENT,
    SET_INNER_HTML,
    SET_ATTRIBUTE,
    REMOVE_ATTRIBUTE,
    ADD_CLASS,
    REMOVE_CLASS,
    TOGGLE_CLASS,
    APPEND_CHILD,
    REMOVE_ELEMENT,
    CLONE_ELEMENT,
    SHOW_ELEMENT,
    HIDE_ELEMENT,

    // Style blocks
    SET_CSS_PROPERTY,
    SET_CSS_VARIABLE,
    ADD_INLINE_STYLE,
    REMOVE_INLINE_STYLE,
    SET_OPACITY,
    SET_TRANSFORM,

    // Animation blocks
    ANIMATE_ELEMENT,
    ADD_TRANSITION,
    TRIGGER_KEYFRAME,
    SCROLL_TO_ELEMENT,
    PLAY_ANIMATION,
    PAUSE_ANIMATION,

    // Data blocks
    DECLARE_VARIABLE,
    SET_VARIABLE,
    GET_VARIABLE,
    LOCAL_STORAGE_SET,
    LOCAL_STORAGE_GET,
    LOCAL_STORAGE_REMOVE,
    SESSION_STORAGE_SET,
    SESSION_STORAGE_GET,
    COOKIE_SET,
    COOKIE_GET,

    // Network blocks
    FETCH_GET,
    FETCH_POST,
    FETCH_PUT,
    FETCH_DELETE,
    ON_SUCCESS_RESPONSE,
    ON_ERROR_RESPONSE,
    PARSE_JSON,

    // Navigation blocks
    NAVIGATE_TO_URL,
    NAVIGATE_TO_PAGE,
    OPEN_IN_NEW_TAB,
    GO_BACK,
    GO_FORWARD,
    SCROLL_TO_TOP,
    OPEN_MODAL,
    CLOSE_MODAL,
    SHOW_TOAST,
    SHOW_ALERT,
    SHOW_CONFIRM,

    // Utility blocks
    CONSOLE_LOG,
    SET_TIMEOUT,
    SET_INTERVAL,
    CLEAR_INTERVAL,
    MATH_OPERATION,
    STRING_OPERATION,
    ARRAY_OPERATION,
    DATE_NOW,
    RANDOM_NUMBER,
    PARSE_INT,
    PARSE_FLOAT,
}

@Serializable
enum class BlockCategory {
    EVENT,
    LOGIC,
    DOM,
    STYLE,
    ANIMATION,
    DATA,
    NETWORK,
    NAVIGATION,
    UTILITY,
}

@Serializable
enum class BlockColorToken {
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    BLUE,
    PURPLE,
}

@Serializable
enum class BlockParameterType {
    TEXT,
    MULTILINE_TEXT,
    NUMBER,
    BOOLEAN,
    COLOR,
    ELEMENT_SELECTOR,
    CSS_PROPERTY_NAME,
    CSS_UNIT_VALUE,
    EVENT_TYPE,
    VARIABLE_REF,
    URL,
    JSON_OBJECT,
    EXPRESSION,
}

@Serializable
data class BlockParameterDefinition(
    val name: String,
    val type: BlockParameterType,
    val label: String,
    val defaultValue: String = "",
    val acceptsVariableReference: Boolean = false,
)

@Serializable
data class BlockTypeDescriptor(
    val displayName: String,
    val category: BlockCategory,
    val colorToken: BlockColorToken,
    val icon: String,
    val eventType: BlockEventType? = null,
    val parameters: List<BlockParameterDefinition> = emptyList(),
    val hasBodySlot: Boolean = false,
    val hasNextConnector: Boolean = true,
    val codeTemplate: String,
)

object BlockDescriptors {

    val all: Map<BlockType, BlockTypeDescriptor> = buildMap {
        // Event
        putEvent(BlockType.ON_CLICK, BlockEventType.ON_CLICK, "On Click", "touch_app", "${'$'}{target}.addEventListener('click', async (event) => {\n${'$'}{body}\n});")
        putEvent(BlockType.ON_LONG_PRESS, BlockEventType.ON_LONG_PRESS, "On Long Press", "back_hand", "// long press event wrapper")
        putEvent(BlockType.ON_HOVER_ENTER, BlockEventType.ON_HOVER_ENTER, "On Hover Enter", "ads_click", "${'$'}{target}.addEventListener('mouseenter', (event) => {\n${'$'}{body}\n});")
        putEvent(BlockType.ON_HOVER_LEAVE, BlockEventType.ON_HOVER_LEAVE, "On Hover Leave", "swipe", "${'$'}{target}.addEventListener('mouseleave', (event) => {\n${'$'}{body}\n});")
        putEvent(BlockType.ON_SCROLL_INTO_VIEW, BlockEventType.ON_SCROLL_INTO_VIEW, "On Scroll Into View", "visibility", "new IntersectionObserver((entries)=>{\n${'$'}{body}\n}).observe(${'$'}{target});")
        putEvent(BlockType.ON_PAGE_LOAD, BlockEventType.ON_PAGE_LOAD, "On Page Load", "home", "window.addEventListener('load', async () => {\n${'$'}{body}\n});")
        putEvent(BlockType.ON_INPUT_CHANGE, BlockEventType.ON_INPUT_CHANGE, "On Input Change", "edit", "${'$'}{target}.addEventListener('input', (event) => {\n${'$'}{body}\n});")
        putEvent(BlockType.ON_FORM_SUBMIT, BlockEventType.ON_FORM_SUBMIT, "On Form Submit", "send", "${'$'}{target}.addEventListener('submit', async (event) => {\nevent.preventDefault();\n${'$'}{body}\n});")
        putEvent(BlockType.ON_KEY_PRESS, BlockEventType.ON_KEY_PRESS, "On Key Press", "keyboard", "window.addEventListener('keydown', (event) => {\n${'$'}{body}\n});")
        putEvent(BlockType.ON_DOUBLE_CLICK, BlockEventType.ON_DOUBLE_CLICK, "On Double Click", "ads_click", "${'$'}{target}.addEventListener('dblclick', (event) => {\n${'$'}{body}\n});")

        // Logic
        putLogic(BlockType.IF_CONDITION, "If Condition", "call_split", true, listOf(exprParam("condition", "Condition", "true")), "if (${'$'}{condition}) {\n${'$'}{body}\n}")
        putLogic(BlockType.ELSE_BRANCH, "Else", "alt_route", true, emptyList(), "else {\n${'$'}{body}\n}")
        putLogic(BlockType.ELSE_IF_BRANCH, "Else If", "alt_route", true, listOf(exprParam("condition", "Condition", "true")), "else if (${'$'}{condition}) {\n${'$'}{body}\n}")
        putLogic(BlockType.FOR_LOOP, "For Loop", "repeat", true, listOf(textParam("initializer", "Initializer", "let i = 0"), exprParam("condition", "Condition", "i < 10"), textParam("increment", "Increment", "i++")), "for (${'$'}{initializer}; ${'$'}{condition}; ${'$'}{increment}) {\n${'$'}{body}\n}")
        putLogic(BlockType.WHILE_LOOP, "While Loop", "sync", true, listOf(exprParam("condition", "Condition", "true")), "while (${'$'}{condition}) {\n${'$'}{body}\n}")
        putLogic(BlockType.SWITCH_CASE, "Switch Case", "account_tree", true, listOf(exprParam("expression", "Expression", "value")), "switch (${'$'}{expression}) {\n${'$'}{body}\n}")
        putLogic(BlockType.CASE_BRANCH, "Case", "subdirectory_arrow_right", true, listOf(textParam("caseValue", "Case Value", "default")), "case ${'$'}{caseValue}:\n${'$'}{body}\nbreak;")
        putLogic(BlockType.TRY_BLOCK, "Try", "security", true, emptyList(), "try {\n${'$'}{body}\n}")
        putLogic(BlockType.CATCH_BLOCK, "Catch", "report_problem", true, listOf(textParam("errorVar", "Error Variable", "error")), "catch (${'$'}{errorVar}) {\n${'$'}{body}\n}")
        putLogic(BlockType.BREAK, "Break", "subdirectory_arrow_left", false, emptyList(), "break;")
        putLogic(BlockType.CONTINUE, "Continue", "redo", false, emptyList(), "continue;")
        putLogic(BlockType.RETURN, "Return", "keyboard_return", false, listOf(textParam("value", "Value", "")), "return ${'$'}{value};")

        // DOM
        putDom(BlockType.GET_ELEMENT_BY_ID, "Get Element by Id", "tag", listOf(textParam("id", "Element Id", "my-element")), "const ${'$'}{resultVar} = document.getElementById(${'$'}{id});")
        putDom(BlockType.GET_ELEMENT_BY_CLASS, "Get Element by Class", "tag", listOf(textParam("className", "Class Name", "item")), "const ${'$'}{resultVar} = document.getElementsByClassName(${'$'}{className});")
        putDom(BlockType.SET_TEXT_CONTENT, "Set Text Content", "title", listOf(elementParam(), textParam("text", "Text", "Hello", true)), "${'$'}{element}.textContent = ${'$'}{text};")
        putDom(BlockType.SET_INNER_HTML, "Set Inner HTML", "code", listOf(elementParam(), multilineParam("html", "HTML", "<b>Hello</b>", true)), "${'$'}{element}.innerHTML = ${'$'}{html};")
        putDom(BlockType.SET_ATTRIBUTE, "Set Attribute", "tune", listOf(elementParam(), textParam("name", "Name", "data-id"), textParam("value", "Value", "1", true)), "${'$'}{element}.setAttribute(${'$'}{name}, ${'$'}{value});")
        putDom(BlockType.REMOVE_ATTRIBUTE, "Remove Attribute", "delete", listOf(elementParam(), textParam("name", "Name", "disabled")), "${'$'}{element}.removeAttribute(${'$'}{name});")
        putDom(BlockType.ADD_CLASS, "Add Class", "add_box", listOf(elementParam(), textParam("className", "Class", "active", true)), "${'$'}{element}.classList.add(${'$'}{className});")
        putDom(BlockType.REMOVE_CLASS, "Remove Class", "indeterminate_check_box", listOf(elementParam(), textParam("className", "Class", "active", true)), "${'$'}{element}.classList.remove(${'$'}{className});")
        putDom(BlockType.TOGGLE_CLASS, "Toggle Class", "swap_horiz", listOf(elementParam(), textParam("className", "Class", "active", true)), "${'$'}{element}.classList.toggle(${'$'}{className});")
        putDom(BlockType.APPEND_CHILD, "Append Child", "call_merge", listOf(elementParam("parent", "Parent"), elementParam("child", "Child")), "${'$'}{parent}.appendChild(${'$'}{child});")
        putDom(BlockType.REMOVE_ELEMENT, "Remove Element", "remove_circle", listOf(elementParam()), "${'$'}{element}.remove();")
        putDom(BlockType.CLONE_ELEMENT, "Clone Element", "content_copy", listOf(elementParam(), boolParam("deep", "Deep Clone", "true")), "const ${'$'}{resultVar} = ${'$'}{element}.cloneNode(${'$'}{deep});")
        putDom(BlockType.SHOW_ELEMENT, "Show Element", "visibility", listOf(elementParam()), "${'$'}{element}.style.display = '';")
        putDom(BlockType.HIDE_ELEMENT, "Hide Element", "visibility_off", listOf(elementParam()), "${'$'}{element}.style.display = 'none';")

        // Style
        putStyle(BlockType.SET_CSS_PROPERTY, "Set CSS Property", "palette", listOf(elementParam(), cssNameParam("property", "Property", "color"), textParam("value", "Value", "#000", true)), "${'$'}{element}.style.setProperty(${'$'}{property}, ${'$'}{value});")
        putStyle(BlockType.SET_CSS_VARIABLE, "Set CSS Variable", "palette", listOf(textParam("name", "Variable Name", "--primary"), textParam("value", "Value", "#6750A4", true)), "document.documentElement.style.setProperty(${'$'}{name}, ${'$'}{value});")
        putStyle(BlockType.ADD_INLINE_STYLE, "Add Inline Style", "format_paint", listOf(elementParam(), multilineParam("style", "Style", "color: red;", true)), "${'$'}{element}.style.cssText += ${'$'}{style};")
        putStyle(BlockType.REMOVE_INLINE_STYLE, "Remove Inline Style", "format_paint", listOf(elementParam(), cssNameParam("property", "Property", "color")), "${'$'}{element}.style.removeProperty(${'$'}{property});")
        putStyle(BlockType.SET_OPACITY, "Set Opacity", "opacity", listOf(elementParam(), numParam("value", "Opacity", "1", true)), "${'$'}{element}.style.opacity = ${'$'}{value};")
        putStyle(BlockType.SET_TRANSFORM, "Set Transform", "crop_rotate", listOf(elementParam(), textParam("transform", "Transform", "translateX(10px)", true)), "${'$'}{element}.style.transform = ${'$'}{transform};")

        // Animation
        putAnimation(BlockType.ANIMATE_ELEMENT, "Animate Element", "animation", listOf(elementParam(), jsonParam("keyframes", "Keyframes", "[{\"opacity\":0},{\"opacity\":1}]"), jsonParam("options", "Options", "{\"duration\":300}")), "${'$'}{element}.animate(${'$'}{keyframes}, ${'$'}{options});")
        putAnimation(BlockType.ADD_TRANSITION, "Add Transition", "auto_awesome_motion", listOf(elementParam(), textParam("value", "Transition", "all 300ms ease", true)), "${'$'}{element}.style.transition = ${'$'}{value};")
        putAnimation(BlockType.TRIGGER_KEYFRAME, "Trigger Keyframe", "movie", listOf(elementParam(), textParam("name", "Animation Name", "pulse", true)), "${'$'}{element}.style.animationName = ${'$'}{name};")
        putAnimation(BlockType.SCROLL_TO_ELEMENT, "Scroll To Element", "south", listOf(elementParam(), boolParam("smooth", "Smooth", "true")), "${'$'}{element}.scrollIntoView({ behavior: ${'$'}{smooth} ? 'smooth' : 'auto' });")
        putAnimation(BlockType.PLAY_ANIMATION, "Play Animation", "play_arrow", listOf(elementParam()), "${'$'}{element}.style.animationPlayState = 'running';")
        putAnimation(BlockType.PAUSE_ANIMATION, "Pause Animation", "pause", listOf(elementParam()), "${'$'}{element}.style.animationPlayState = 'paused';")

        // Data
        putData(BlockType.DECLARE_VARIABLE, "Declare Variable", "data_object", listOf(textParam("name", "Name", "value"), textParam("value", "Default Value", "", true)), "let ${'$'}{name} = ${'$'}{value};")
        putData(BlockType.SET_VARIABLE, "Set Variable", "edit_note", listOf(variableParam("name", "Variable", "value"), textParam("value", "Value", "", true)), "${'$'}{name} = ${'$'}{value};")
        putData(BlockType.GET_VARIABLE, "Get Variable", "saved_search", listOf(variableParam("name", "Variable", "value")), "${'$'}{name}")
        putData(BlockType.LOCAL_STORAGE_SET, "LocalStorage Set", "save", listOf(textParam("key", "Key", "key"), textParam("value", "Value", "", true)), "localStorage.setItem(${'$'}{key}, ${'$'}{value});")
        putData(BlockType.LOCAL_STORAGE_GET, "LocalStorage Get", "manage_search", listOf(textParam("key", "Key", "key"), textParam("fallback", "Fallback", "null")), "const ${'$'}{resultVar} = localStorage.getItem(${'$'}{key}) ?? ${'$'}{fallback};")
        putData(BlockType.LOCAL_STORAGE_REMOVE, "LocalStorage Remove", "delete", listOf(textParam("key", "Key", "key")), "localStorage.removeItem(${'$'}{key});")
        putData(BlockType.SESSION_STORAGE_SET, "SessionStorage Set", "save_as", listOf(textParam("key", "Key", "key"), textParam("value", "Value", "", true)), "sessionStorage.setItem(${'$'}{key}, ${'$'}{value});")
        putData(BlockType.SESSION_STORAGE_GET, "SessionStorage Get", "manage_search", listOf(textParam("key", "Key", "key"), textParam("fallback", "Fallback", "null")), "const ${'$'}{resultVar} = sessionStorage.getItem(${'$'}{key}) ?? ${'$'}{fallback};")
        putData(BlockType.COOKIE_SET, "Cookie Set", "cookie", listOf(textParam("key", "Key", "token"), textParam("value", "Value", "", true)), "document.cookie = `${'$'}{key}=${'$'}{value}`;")
        putData(BlockType.COOKIE_GET, "Cookie Get", "cookie", listOf(textParam("key", "Key", "token")), "const ${'$'}{resultVar} = document.cookie;")

        // Network
        putNetwork(BlockType.FETCH_GET, "Fetch GET", "cloud_download", false, listOf(urlParam("url", "URL", "https://example.com")), "const ${'$'}{responseVar} = await fetch(${'$'}{url});")
        putNetwork(BlockType.FETCH_POST, "Fetch POST", "upload", false, listOf(urlParam("url", "URL", "https://example.com"), jsonParam("body", "Body", "{}")), "const ${'$'}{responseVar} = await fetch(${'$'}{url}, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(${'$'}{body}) });")
        putNetwork(BlockType.FETCH_PUT, "Fetch PUT", "sync_alt", false, listOf(urlParam("url", "URL", "https://example.com"), jsonParam("body", "Body", "{}")), "const ${'$'}{responseVar} = await fetch(${'$'}{url}, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(${'$'}{body}) });")
        putNetwork(BlockType.FETCH_DELETE, "Fetch DELETE", "delete_forever", false, listOf(urlParam("url", "URL", "https://example.com")), "const ${'$'}{responseVar} = await fetch(${'$'}{url}, { method: 'DELETE' });")
        putNetwork(BlockType.ON_SUCCESS_RESPONSE, "On Success", "check_circle", true, listOf(exprParam("condition", "Condition", "response.ok")), "if (${'$'}{condition}) {\n${'$'}{body}\n}")
        putNetwork(BlockType.ON_ERROR_RESPONSE, "On Error", "error", true, listOf(exprParam("condition", "Condition", "!response.ok")), "if (${'$'}{condition}) {\n${'$'}{body}\n}")
        putNetwork(BlockType.PARSE_JSON, "Parse JSON", "data_object", false, listOf(textParam("source", "Source Variable", "response")), "const ${'$'}{resultVar} = await ${'$'}{source}.json();")

        // Navigation
        putNav(BlockType.NAVIGATE_TO_URL, "Navigate To URL", "language", listOf(urlParam("url", "URL", "https://example.com")), "window.location.href = ${'$'}{url};")
        putNav(BlockType.NAVIGATE_TO_PAGE, "Navigate To Page", "arrow_forward", listOf(textParam("path", "Path", "/about")), "window.location.href = ${'$'}{path};")
        putNav(BlockType.OPEN_IN_NEW_TAB, "Open In New Tab", "open_in_new", listOf(urlParam("url", "URL", "https://example.com")), "window.open(${'$'}{url}, '_blank');")
        putNav(BlockType.GO_BACK, "Go Back", "arrow_back", emptyList(), "window.history.back();")
        putNav(BlockType.GO_FORWARD, "Go Forward", "arrow_forward", emptyList(), "window.history.forward();")
        putNav(BlockType.SCROLL_TO_TOP, "Scroll To Top", "vertical_align_top", emptyList(), "window.scrollTo({ top: 0, behavior: 'smooth' });")
        putNav(BlockType.OPEN_MODAL, "Open Modal", "web_asset", listOf(elementParam("modal", "Modal Element")), "${'$'}{modal}.showModal?.();")
        putNav(BlockType.CLOSE_MODAL, "Close Modal", "close", listOf(elementParam("modal", "Modal Element")), "${'$'}{modal}.close?.();")
        putNav(BlockType.SHOW_TOAST, "Show Toast", "chat", listOf(textParam("message", "Message", "Done", true)), "console.log('TOAST:', ${'$'}{message});")
        putNav(BlockType.SHOW_ALERT, "Show Alert", "notification_important", listOf(textParam("message", "Message", "Alert", true)), "window.alert(${'$'}{message});")
        putNav(BlockType.SHOW_CONFIRM, "Show Confirm", "help", listOf(textParam("message", "Message", "Are you sure?", true)), "const ${'$'}{resultVar} = window.confirm(${'$'}{message});")

        // Utility
        putUtility(BlockType.CONSOLE_LOG, "Console Log", "terminal", listOf(textParam("message", "Message", "value", true)), false, "console.log(${'$'}{message});")
        putUtility(BlockType.SET_TIMEOUT, "Set Timeout", "timer", listOf(numParam("delay", "Delay (ms)", "300", true)), true, "setTimeout(() => {\n${'$'}{body}\n}, ${'$'}{delay});")
        putUtility(BlockType.SET_INTERVAL, "Set Interval", "timer_10", listOf(numParam("delay", "Delay (ms)", "1000", true)), true, "const ${'$'}{intervalVar} = setInterval(() => {\n${'$'}{body}\n}, ${'$'}{delay});")
        putUtility(BlockType.CLEAR_INTERVAL, "Clear Interval", "timer_off", listOf(variableParam("name", "Interval Variable", "intervalId")), false, "clearInterval(${'$'}{name});")
        putUtility(BlockType.MATH_OPERATION, "Math Operation", "calculate", listOf(textParam("left", "Left", "1", true), textParam("operator", "Operator", "+"), textParam("right", "Right", "2", true)), false, "const ${'$'}{resultVar} = ${'$'}{left} ${'$'}{operator} ${'$'}{right};")
        putUtility(BlockType.STRING_OPERATION, "String Operation", "text_fields", listOf(textParam("value", "Value", "text", true), textParam("operation", "Operation", "toUpperCase")), false, "const ${'$'}{resultVar} = ${'$'}{value}.${'$'}{operation}();")
        putUtility(BlockType.ARRAY_OPERATION, "Array Operation", "view_list", listOf(textParam("array", "Array", "items", true), textParam("operation", "Operation", "push"), textParam("value", "Value", "item", true)), false, "${'$'}{array}.${'$'}{operation}(${'$'}{value});")
        putUtility(BlockType.DATE_NOW, "Date Now", "today", emptyList(), false, "const ${'$'}{resultVar} = Date.now();")
        putUtility(BlockType.RANDOM_NUMBER, "Random Number", "casino", listOf(numParam("min", "Min", "0", true), numParam("max", "Max", "1", true)), false, "const ${'$'}{resultVar} = Math.random() * (${'$'}{max} - ${'$'}{min}) + ${'$'}{min};")
        putUtility(BlockType.PARSE_INT, "Parse Int", "pin", listOf(textParam("value", "Value", "0", true)), false, "const ${'$'}{resultVar} = parseInt(${'$'}{value}, 10);")
        putUtility(BlockType.PARSE_FLOAT, "Parse Float", "pin", listOf(textParam("value", "Value", "0", true)), false, "const ${'$'}{resultVar} = parseFloat(${'$'}{value});")
    }

    fun byCategory(): Map<BlockCategory, List<Pair<BlockType, BlockTypeDescriptor>>> =
        all.entries
            .groupBy({ it.value.category }, { it.key to it.value })
            .mapValues { (_, values) -> values.sortedBy { it.second.displayName } }

    fun descriptorForEvent(eventType: BlockEventType): BlockTypeDescriptor =
        all.values.firstOrNull { it.category == BlockCategory.EVENT && it.eventType == eventType }
            ?: all.getValue(BlockType.ON_CLICK)

    private fun MutableMap<BlockType, BlockTypeDescriptor>.putEvent(
        type: BlockType,
        eventType: BlockEventType,
        name: String,
        icon: String,
        template: String,
    ) {
        put(
            type,
            BlockTypeDescriptor(
                displayName = name,
                category = BlockCategory.EVENT,
                colorToken = BlockColorToken.PURPLE,
                icon = icon,
                eventType = eventType,
                hasBodySlot = true,
                hasNextConnector = false,
                codeTemplate = template,
            ),
        )
    }

    private fun MutableMap<BlockType, BlockTypeDescriptor>.putLogic(
        type: BlockType,
        name: String,
        icon: String,
        hasBody: Boolean,
        parameters: List<BlockParameterDefinition>,
        template: String,
    ) {
        put(type, BlockTypeDescriptor(name, BlockCategory.LOGIC, BlockColorToken.ORANGE, icon, null, parameters, hasBody, true, template))
    }

    private fun MutableMap<BlockType, BlockTypeDescriptor>.putDom(
        type: BlockType,
        name: String,
        icon: String,
        parameters: List<BlockParameterDefinition>,
        template: String,
    ) {
        put(type, BlockTypeDescriptor(name, BlockCategory.DOM, BlockColorToken.BLUE, icon, null, parameters, false, true, template))
    }

    private fun MutableMap<BlockType, BlockTypeDescriptor>.putStyle(
        type: BlockType,
        name: String,
        icon: String,
        parameters: List<BlockParameterDefinition>,
        template: String,
    ) {
        put(type, BlockTypeDescriptor(name, BlockCategory.STYLE, BlockColorToken.GREEN, icon, null, parameters, false, true, template))
    }

    private fun MutableMap<BlockType, BlockTypeDescriptor>.putAnimation(
        type: BlockType,
        name: String,
        icon: String,
        parameters: List<BlockParameterDefinition>,
        template: String,
    ) {
        put(type, BlockTypeDescriptor(name, BlockCategory.ANIMATION, BlockColorToken.YELLOW, icon, null, parameters, false, true, template))
    }

    private fun MutableMap<BlockType, BlockTypeDescriptor>.putData(
        type: BlockType,
        name: String,
        icon: String,
        parameters: List<BlockParameterDefinition>,
        template: String,
    ) {
        put(type, BlockTypeDescriptor(name, BlockCategory.DATA, BlockColorToken.RED, icon, null, parameters, false, true, template))
    }

    private fun MutableMap<BlockType, BlockTypeDescriptor>.putNetwork(
        type: BlockType,
        name: String,
        icon: String,
        hasBody: Boolean,
        parameters: List<BlockParameterDefinition>,
        template: String,
    ) {
        put(type, BlockTypeDescriptor(name, BlockCategory.NETWORK, BlockColorToken.PURPLE, icon, null, parameters, hasBody, true, template))
    }

    private fun MutableMap<BlockType, BlockTypeDescriptor>.putNav(
        type: BlockType,
        name: String,
        icon: String,
        parameters: List<BlockParameterDefinition>,
        template: String,
    ) {
        put(type, BlockTypeDescriptor(name, BlockCategory.NAVIGATION, BlockColorToken.BLUE, icon, null, parameters, false, true, template))
    }

    private fun MutableMap<BlockType, BlockTypeDescriptor>.putUtility(
        type: BlockType,
        name: String,
        icon: String,
        parameters: List<BlockParameterDefinition>,
        hasBody: Boolean,
        template: String,
    ) {
        put(type, BlockTypeDescriptor(name, BlockCategory.UTILITY, BlockColorToken.GREEN, icon, null, parameters, hasBody, true, template))
    }

    private fun textParam(name: String, label: String, default: String, varRef: Boolean = false) =
        BlockParameterDefinition(name, BlockParameterType.TEXT, label, default, varRef)

    private fun multilineParam(name: String, label: String, default: String, varRef: Boolean = false) =
        BlockParameterDefinition(name, BlockParameterType.MULTILINE_TEXT, label, default, varRef)

    private fun numParam(name: String, label: String, default: String, varRef: Boolean = false) =
        BlockParameterDefinition(name, BlockParameterType.NUMBER, label, default, varRef)

    private fun boolParam(name: String, label: String, default: String) =
        BlockParameterDefinition(name, BlockParameterType.BOOLEAN, label, default, false)

    private fun exprParam(name: String, label: String, default: String) =
        BlockParameterDefinition(name, BlockParameterType.EXPRESSION, label, default, true)

    private fun variableParam(name: String, label: String, default: String) =
        BlockParameterDefinition(name, BlockParameterType.VARIABLE_REF, label, default, true)

    private fun urlParam(name: String, label: String, default: String) =
        BlockParameterDefinition(name, BlockParameterType.URL, label, default, false)

    private fun jsonParam(name: String, label: String, default: String) =
        BlockParameterDefinition(name, BlockParameterType.JSON_OBJECT, label, default, false)

    private fun cssNameParam(name: String, label: String, default: String) =
        BlockParameterDefinition(name, BlockParameterType.CSS_PROPERTY_NAME, label, default, false)

    private fun elementParam(name: String = "element", label: String = "Element") =
        BlockParameterDefinition(name, BlockParameterType.ELEMENT_SELECTOR, label, "event.target", true)
}

val BlockCategory.displayName: String
    get() = name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() }

val BlockEventType.displayName: String
    get() = name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() }
