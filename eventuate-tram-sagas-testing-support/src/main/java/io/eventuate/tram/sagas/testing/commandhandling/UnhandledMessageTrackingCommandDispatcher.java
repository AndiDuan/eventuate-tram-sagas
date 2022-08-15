package io.eventuate.tram.sagas.testing.commandhandling;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandHandler;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class UnhandledMessageTrackingCommandDispatcher extends CommandDispatcher {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private CommandHandlers commandHandlers;
  private List<Message> unhandledMessages = new LinkedList<>();

  public UnhandledMessageTrackingCommandDispatcher(String commandDispatcherId,
                                                   CommandHandlers commandHandlers,
                                                   MessageConsumer messageConsumer,
                                                   CommandNameMapping commandNameMapping, CommandReplyProducer commandReplyProducer) {
    super(commandDispatcherId, commandHandlers, messageConsumer, commandNameMapping, commandReplyProducer);
    this.commandHandlers = commandHandlers;
  }

  @Override
  public void messageHandler(Message message) {
    Optional<CommandHandler> possibleMethod = commandHandlers.findTargetMethod(message);
    if (possibleMethod.isPresent())
      super.messageHandler(message);
    else {
      logger.info("unhandled message {}", message);
      unhandledMessages.add(message);

    }
  }


  public void reset() {
    unhandledMessages.clear();
  }

  public <C extends Command> void noteNewCommandHandler(SagaParticipantStubCommandHandler<C> commandHandler) {
    List<Message> handled = unhandledMessages.stream().filter(commandHandler::handles).collect(toList());
    if (!handled.isEmpty())
      logger.info("Processing unhandled messages {}", handled);
    unhandledMessages.removeAll(handled);
    handled.forEach(super::messageHandler);
  }
}
