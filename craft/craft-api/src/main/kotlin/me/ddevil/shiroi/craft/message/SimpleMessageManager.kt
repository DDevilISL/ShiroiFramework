package me.ddevil.shiroi.craft.message

import me.ddevil.shiroi.craft.config.FileConfigManager
import me.ddevil.shiroi.craft.config.FileConfigSource
import me.ddevil.shiroi.craft.config.FileConfigValue
import me.ddevil.shiroi.craft.misc.variable.VariableProvider
import me.ddevil.shiroi.craft.plugin.ShiroiPlugin
import me.ddevil.shiroi.util.DEFAULT_SHIROI_COLOR_CHAR
import me.ddevil.shiroi.util.DEFAULT_SHIROI_DESIGN_COLOR_CHAR
import me.ddevil.shiroi.util.misc.design.MinecraftColor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender


open class SimpleMessageManager
@JvmOverloads
constructor(
        plugin: ShiroiPlugin<*, *>,
        val messageSeparator: String,
        val pluginPrefix: String,
        providers: List<VariableProvider> = emptyList()
) : AbstractMessageManager(plugin) {
    override fun disable() {

    }

    final override fun broadcastMessage(vararg messages: String) = messages.forEach {
        Bukkit.broadcastMessage(translateAll("$pluginPrefix$messageSeparator$it"))
    }

    override fun isValidColor(i: Char): Boolean {
        if (!i.isDigit()) {
            return false
        }
        return Character.getNumericValue(i) in 1..5
    }

    val providers: List<VariableProvider>

    init {
        val reservedNames = arrayOf(
                PluginPrefixVariableProvider.PREFIX_VARIABLE_NAME,
                MessageSeparatorVariableProvider.SEPARATOR_VARIABLE_NAME
        )
        this.providers = listOf(
                *providers.filter { !it.provide().any { va -> reservedNames.contains(va.name) } }.toTypedArray(),
                PluginPrefixVariableProvider(pluginPrefix),
                MessageSeparatorVariableProvider(messageSeparator)
        )
    }


    override fun get(color: MessageColor): Char {
        when (color) {
            MessageColor.PRIMARY -> return colorDesign.primaryColor.char
            MessageColor.SECONDARY -> return colorDesign.secondaryColor.char
            MessageColor.NEUTRAL -> return colorDesign.neutralColor.char
            MessageColor.WARNING -> return colorDesign.warningColor.char
            MessageColor.SUCCESS -> return colorDesign.successColor.char
        }
    }

    override fun sendMessage(p: CommandSender, vararg messages: String) {
        for (message in messages) {
            p.sendMessage(translateAll("$pluginPrefix$messageSeparator$message"))
        }
    }

    override fun translateTags(input: String): String {
        var final = input
        for (translator in providers) {
            val variables = translator.provide()
            for (variable in variables) {
                val tag = variable.replacer
                if (final.contains(tag)) {
                    final = final.replace(tag, variable.value)
                }
            }
        }
        return final
    }

    override fun translateColors(input: String): String {
        val b = input.toCharArray()
        for (i in 0..b.size - 1) {
            if (b[i] == DEFAULT_SHIROI_DESIGN_COLOR_CHAR && isValidColor(b[i + 1])) {
                b[i] = ChatColor.COLOR_CHAR
                b[i + 1] = get(Character.getNumericValue(b[i + 1]))
            }
        }
        return MinecraftColor.translateAlternateColorCodes(kotlin.text.String(b), DEFAULT_SHIROI_COLOR_CHAR)
    }

    override fun enable() {
    }

    override fun get(i: Int): Char {
        when (i) {
            1 -> return colorDesign.primaryColor.char
            2 -> return colorDesign.secondaryColor.char
            3 -> return colorDesign.neutralColor.char
            4 -> return colorDesign.warningColor.char
            5 -> return colorDesign.successColor.char
            else -> throw IllegalArgumentException("Illegal color char '$i'!")
        }
    }

    companion object {
        @JvmOverloads
        fun create(plugin: ShiroiPlugin<*, *>,
                   messageSeparator: String,
                   pluginPrefix: String,
                   providers: List<VariableProvider> = emptyList()
        ) = SimpleMessageManager(plugin, messageSeparator, pluginPrefix, providers)

        @JvmOverloads
        fun <K : FileConfigSource> create(
                plugin: ShiroiPlugin<*, *>,
                configManager: FileConfigManager<K, *>,
                pluginPrefix: FileConfigValue<String, K>,
                messageSeparator: FileConfigValue<String, K>,
                providers: List<VariableProvider> = emptyList()
        ) = create(plugin,
                configManager.getValue(pluginPrefix),
                configManager.getValue(messageSeparator),
                providers)

    }
}

