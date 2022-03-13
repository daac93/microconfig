package io.microconfig.core.exceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExceptionAccumulator {

    private static Map<String, List<MicroconfigException>> exceptionsMap =
            Collections.synchronizedMap(new HashMap<>());

    public static void addException(String component, MicroconfigException e) {
        if (exceptionsMap.containsKey(component))   {
            exceptionsMap.get(component).add(e);
        }   else    {
            List<MicroconfigException> newExceptionList = new ArrayList<>();
            newExceptionList.add(e);
            exceptionsMap.put(component, newExceptionList);
        }

    }

    public static Map<String, List<MicroconfigException>> getExceptionsMap()    {
        return exceptionsMap;
    }

    public static void clearExceptions() {
        exceptionsMap.clear();
    }

    public static boolean hasNoExceptions()    {
        return exceptionsMap.isEmpty();
    }

    public static boolean hasAnyExceptions()    {
        return !hasNoExceptions();
    }


}
