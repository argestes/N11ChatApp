package tr.yigitunlu.n11chatapp.domain.model

/**
 * Represents the current state of the chat
 */
enum class ChatState(i: Int) {
    NotStarted(0),
    WaitUserMessage(2),
    Terminated(3)
}
