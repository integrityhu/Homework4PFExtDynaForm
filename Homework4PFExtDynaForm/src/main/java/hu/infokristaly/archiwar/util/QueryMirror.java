package hu.infokristaly.archiwar.util;

import hu.infokristaly.archiwar.front.annotations.QueryFieldInfo;
import hu.infokristaly.archiwar.front.annotations.QueryInfo;

public class QueryMirror {

    @QueryInfo(fields={@QueryFieldInfo()})
    public String query;
    
}
