package net.i18ndev.spring.batch.prototypes.threadlevelreading;

import jdk.jfr.BooleanFlag;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

public class DebugChunkListener implements ChunkListener {

    @Override
    public void beforeChunk(ChunkContext context) {
        System.out.println("Before chunk: Thread: " + Thread.currentThread().getName() + " Chunk: "+ context);
    }

    @Override
    public void afterChunk(ChunkContext context) {
        System.out.println("After chunk: Thread: " + Thread.currentThread().getName() + " Chunk: "+ context);
    }

}
