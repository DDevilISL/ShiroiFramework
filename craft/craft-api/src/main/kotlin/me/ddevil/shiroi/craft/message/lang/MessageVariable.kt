package me.ddevil.shiroi.craft.message.lang

class MessageVariable(val name: String, val value: String) {
    val replacer: String = "{$name}"
}
