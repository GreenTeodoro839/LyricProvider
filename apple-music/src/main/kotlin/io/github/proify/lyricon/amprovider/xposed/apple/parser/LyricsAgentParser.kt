package io.github.proify.lyricon.amprovider.xposed.apple.parser

import io.github.proify.lyricon.amprovider.xposed.apple.model.LyricAgent

object LyricsAgentParser : Parser() {

    fun parserAgentVector(any: Any): MutableList<LyricAgent> {
        val agents = mutableListOf<LyricAgent>()
        val size = get(any, "size") as? Long ?: 0
        for (i in 0..<size) {
            val agentPtr: Any? = get(any, "get", i)
            val agentNative: Any? = agentPtr?.let { get(it, "get") }
            val agent = agentNative?.let { parserAgentNative(it) }
            agent?.let { agents.add(it) }
        }
        return agents
    }

    fun parserAgentNative(agentNative: Any): LyricAgent {
        val agent = LyricAgent()
        agent.nameTypes = get(agentNative, "getNameTypes_") as? IntArray ?: intArrayOf()
        agent.type = get(agentNative, "getType_") as? Long ?: 0
        agent.id = get(agentNative, "getId") as? String
        agent.nameTypeNames = LyricAgent.getNameTypesNames(agent.nameTypes)
        agent.typeName = LyricAgent.getType(agent.type)?.name
        return agent
    }

}