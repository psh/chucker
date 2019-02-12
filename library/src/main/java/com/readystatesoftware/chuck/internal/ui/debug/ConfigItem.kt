package com.readystatesoftware.chuck.internal.ui.debug

import com.readystatesoftware.chuck.api.MockedResponse
import com.readystatesoftware.chuck.internal.debug.DebuggingChainProcessor

const val THROTTLING_HEADER_TYPE = 1
const val THROTTLING_EDITOR_TYPE = 2
const val ACTIVE_MOCKS_HEADER_TYPE = 3
const val ACTIVE_MOCKS_TYPE = 4
const val INACTIVE_MOCKS_HEADER_TYPE = 5
const val INACTIVE_MOCKS_TYPE = 6

interface ConfigItem {
    val id: String
    val type: Int
}

class InactiveMock(val mock: MockedResponse) : ConfigItem {
    override val type = INACTIVE_MOCKS_TYPE
    override val id: String
        get() = mock.id
}

class InactiveMocksHeader : ConfigItem {
    override val type = INACTIVE_MOCKS_HEADER_TYPE
    override val id = "_inactive_mocks_header_"
}

class ActiveMock(val mock: MockedResponse) : ConfigItem {
    override val type = ACTIVE_MOCKS_TYPE
    override val id: String
        get() = mock.id
}

class ActiveMocksHeader : ConfigItem {
    override val type = ACTIVE_MOCKS_HEADER_TYPE
    override val id = "_active_mocks_header_"
}

class ThrottlingModel(val chainProcessor: DebuggingChainProcessor) : ConfigItem {
    override val type = THROTTLING_EDITOR_TYPE
    override val id = "_throttling_model_"

}

class ThrottlingHeaderItem : ConfigItem {
    override val type = THROTTLING_HEADER_TYPE
    override val id = "_throttling_header_"
}
