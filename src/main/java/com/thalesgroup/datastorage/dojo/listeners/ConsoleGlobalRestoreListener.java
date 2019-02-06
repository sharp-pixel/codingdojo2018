package com.thalesgroup.datastorage.dojo.listeners;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.processor.StateRestoreListener;

@Slf4j
public class ConsoleGlobalRestoreListener implements StateRestoreListener {

    @Override
    public void onRestoreStart(final TopicPartition topicPartition,
                               final String storeName,
                               final long startingOffset,
                               final long endingOffset) {

        log.trace("Started restoration of {} partition {}", storeName, topicPartition.partition());
        log.trace(" total records to be restored {}", endingOffset - startingOffset);
    }

    @Override
    public void onBatchRestored(final TopicPartition topicPartition,
                                final String storeName,
                                final long batchEndOffset,
                                final long numRestored) {

//        System.out.println(String.format("Restored batch %s for %s partition %s", numRestored, storeName, topicPartition.partition()));
    }

    @Override
    public void onRestoreEnd(final TopicPartition topicPartition,
                             final String storeName,
                             final long totalRestored) {

        log.trace("Restoration complete for {} partition {}", storeName, topicPartition.partition());
    }
}
