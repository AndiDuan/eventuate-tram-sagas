package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.CommandWithDestinationAndType;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ReactiveParticipantInvocationImpl<Data, C extends Command> extends AbstractReactiveParticipantInvocation<Data> {
  private Function<Data, Publisher<CommandWithDestination>> commandBuilder;
  private final boolean notification;


  public ReactiveParticipantInvocationImpl(Optional<Predicate<Data>> invocablePredicate,
                                           Function<Data, Publisher<CommandWithDestination>> commandBuilder) {
    super(invocablePredicate);
    this.commandBuilder = commandBuilder;
    this.notification = false;
  }

  public ReactiveParticipantInvocationImpl(Optional<Predicate<Data>> invocablePredicate, Function<Data, Publisher<CommandWithDestination>> commandBuilder, boolean notification) {
    super(invocablePredicate);
    this.commandBuilder = commandBuilder;
    this.notification = notification;
  }

  @Override
  public boolean isSuccessfulReply(Message message) {
    return CommandReplyOutcome.SUCCESS.name().equals(message.getRequiredHeader(ReplyMessageHeaders.REPLY_OUTCOME));
  }

  @Override
  public Publisher<CommandWithDestinationAndType> makeCommandToSend(Data data) {
    return Mono.from(commandBuilder.apply(data)).map(cmd -> new CommandWithDestinationAndType(cmd, notification));
  }
}
