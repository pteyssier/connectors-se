package org.talend.components.zendesk.helpers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.talend.components.zendesk.sources.Reject;
import org.talend.sdk.component.api.processor.OutputEmitter;

import javax.json.JsonObject;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class CommonHelper {

    public static void processException(Exception e, JsonObject record, OutputEmitter<Reject> reject) {
        log.warn(e.getMessage());
        reject.emit(new Reject(e.getMessage(), record));
    }

    public static long[] arrayOfObjectsToPrimitives(Long... objects) {
        long[] primitives = new long[objects.length];
        for (int i = 0; i < objects.length; i++)
            primitives[i] = objects[i];
        return primitives;
    }

}
