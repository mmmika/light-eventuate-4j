package com.networknt.eventuate.client;

import com.networknt.eventuate.common.EntityIdAndType;
import com.networknt.eventuate.common.EventContext;
import com.networknt.eventuate.common.impl.EventIdTypeAndData;
import com.networknt.eventuate.common.impl.SerializedEvent;
import com.networknt.eventuate.jdbc.AbstractEventuateJdbcAggregateStore;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class EventuateLocalPublishAggregateCrud extends AbstractEventuateJdbcAggregateStore
{

  private AtomicLong eventOffset = new AtomicLong();
  private final Map<EntityIdAndType, List<SerializedEvent>> localEventsMap = new HashMap<EntityIdAndType, List<SerializedEvent>>();

  public EventuateLocalPublishAggregateCrud(DataSource dataSource) {
    super(dataSource);
  }

  protected void publish(String aggregateType, String aggregateId, List<EventIdTypeAndData> eventsWithIds) {
    EntityIdAndType entityIdAndType = new EntityIdAndType(aggregateId, aggregateType);
    localEventsMap.put(entityIdAndType, eventsWithIds.stream().map(item->toSerializedEvent(item, aggregateType, aggregateId)).collect(Collectors.toList()));
    //TODO publish the events to Kafka
  }

  private SerializedEvent toSerializedEvent(EventIdTypeAndData event, String aggregateType, String aggregateId) {
    return new SerializedEvent(event.getId(), aggregateId, aggregateType, event.getEventData(), event.getEventType(),
            aggregateId.hashCode() % 8,
            eventOffset.getAndIncrement(),
            new EventContext(event.getId().asString()));
  }


}