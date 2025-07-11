package com.moji.musicdistribution.config;

import com.moji.musicdistribution.command.CommandBus;
import com.moji.musicdistribution.command.commands.*;
import com.moji.musicdistribution.command.handlers.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for the CQRS architecture components
 */
@Configuration
@EnableScheduling
public class CqrsConfiguration {

    /**
     * Register command handlers with the CommandBus
     */
    @Bean
    public CommandBus configureCommandBus(
            CreateReleaseHandler createReleaseHandler,
            AddSongsHandler addSongsHandler,
            ProposeDateHandler proposeDateHandler,
            ApproveDateHandler approveDateHandler,
            PublishHandler publishHandler,
            StreamHandler streamHandler,
            PaymentReportHandler paymentReportHandler,
            WithdrawHandler withdrawHandler
    ) {
        CommandBus commandBus = new CommandBus();

        // Register handlers that return results
        commandBus.register(CreateRelease.class, createReleaseHandler::handle);

        // Register void handlers
        commandBus.register(AddSongsToRelease.class, addSongsHandler::handle);
        commandBus.register(ProposeReleaseDate.class, proposeDateHandler::handle);
        commandBus.register(ApproveReleaseDate.class, approveDateHandler::handle);
        commandBus.register(PublishRelease.class, publishHandler::handle);
        commandBus.register(RecordStream.class, streamHandler::handle);
        commandBus.register(RequestPaymentReport.class, paymentReportHandler::handle);
        commandBus.register(WithdrawRelease.class, withdrawHandler::handle);

        return commandBus;
    }
}