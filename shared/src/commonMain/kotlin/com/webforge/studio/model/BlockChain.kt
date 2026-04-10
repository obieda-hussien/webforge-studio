package com.webforge.studio.model

/**
 * Placeholder for the visual logic / block-chain system.
 *
 * A [BlockChain] is a sequence of [LogicBlock] nodes wired together to
 * describe event-driven behaviour (e.g. "OnClick → Navigate → ShowToast").
 * This data model is intentionally minimal for Phase 1; it will be
 * expanded in a later phase with proper block types, ports, and wiring.
 *
 * @param id     Unique chain identifier.
 * @param name   Human-readable name shown in the logic editor.
 * @param blocks Ordered sequence of logic blocks in this chain.
 */
data class BlockChain(
    val id: String,
    val name: String,
    val blocks: List<LogicBlock> = emptyList(),
)

/**
 * A single node inside a [BlockChain].
 *
 * @param id         Unique block identifier.
 * @param blockType  The category / kind of action this block performs.
 * @param parameters Key-value parameters specific to [blockType].
 */
data class LogicBlock(
    val id: String,
    val blockType: BlockType,
    val parameters: Map<String, String> = emptyMap(),
)

/** Supported logic-block categories (placeholder list for Phase 1). */
enum class BlockType {
    TRIGGER,
    NAVIGATE,
    SHOW_TOAST,
    HTTP_REQUEST,
    CONDITIONAL,
    CUSTOM,
}
