package org.openea.eap.module.message.support.queue;

public interface MessageQueue {
    EnqueueResult enqueue(MessageTask task);

    class EnqueueResult {
        private final Long messageId;
        private final boolean dedupUsed;

        public EnqueueResult(Long messageId, boolean dedupUsed) {
            this.messageId = messageId;
            this.dedupUsed = dedupUsed;
        }

        public Long getMessageId() {return messageId;}
        public boolean isDedupUsed() {return dedupUsed;}
    }
}
