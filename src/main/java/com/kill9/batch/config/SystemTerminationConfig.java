package com.kill9.batch.config;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SystemTerminationConfig {
    private AtomicInteger processKilled = new AtomicInteger(0);
    private final int TERMINATION_TARGET = 5;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    /**
     * 시뮬레이션 시나리오
     * <p>
     * 1. 세계에 입장 (enterWorldStep)
     * <p>
     * 2. 시스템 관리자 NPC 만남 (meetNPCStep)
     * <p>
     * 3. 프로세스 처형 미션 수행 (defeatProcessStep)
     * <p>
     * 4. 미션 완료 보고 (completeQuestStep)
     */
    @Bean
    public Job systemTerminationSimulationJob() {
        return new JobBuilder("systemTerminationSimulationJob", jobRepository)
                .start(enterWorldStep())
                .next(meetNPCStep())
                .next(defeatProcessStep())
                .next(completeQuestStep())
                .build();
    }

    public Step enterWorldStep() {
        return new StepBuilder("enterWorldStep", jobRepository) // jobRepository에 전달하여 Step 실행 정보 관리
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("System Termination 시뮬레이션 세계에 접속했습니다!");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    public Step meetNPCStep() {
        return new StepBuilder("meetNPCStep", jobRepository) // jobRepository에 전달하여 Step 실행 정보 관리
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("시스템 관리자 NPC를 만났습니다.");
                    System.out.println("첫 번째 미션: 좀비 프로세스 " + TERMINATION_TARGET + "개 처형하기");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    public Step defeatProcessStep() {
        return new StepBuilder("defeatProcessStep", jobRepository) // jobRepository에 전달하여 Step 실행 정보 관리
                .tasklet((contribution, chunkContext) -> {
                    int terminated = processKilled.incrementAndGet();
                    System.out.println("좀비 프로세스 처형 완료! (" + terminated + "/" + TERMINATION_TARGET + ")");
                    if (terminated < TERMINATION_TARGET) {
                        return RepeatStatus.CONTINUABLE; // Step의 뱐복 여부 결정
                    } else {
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager)
                .build();
    }

    @Bean
    public Step completeQuestStep() {
        return new StepBuilder("completeQuestStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("미션완료! 좀비프로세스 " + TERMINATION_TARGET + "개 처형 성공!");
                    System.out.println("보상: KILL -9 권한 획득, 시스템 제어 레벨 1 달성");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
