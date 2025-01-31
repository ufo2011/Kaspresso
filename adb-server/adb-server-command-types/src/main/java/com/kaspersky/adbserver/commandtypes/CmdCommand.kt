package com.kaspersky.adbserver.commandtypes

import com.kaspersky.adbserver.common.api.ComplexCommand

data class CmdCommand(
    override val body: String,
    override val arguments: List<String> = emptyList()
) : ComplexCommand(body, arguments)
