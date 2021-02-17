package pe.albatross.octavia.dynatable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DynatableFilter {

    private Integer page;
    private Integer perPage;
    private Integer offset;
    private Integer total;
    private Integer filtered;
    private Map sorts;
    private Map<String, Object> queries;

    public DynatableFilter() {
        sorts = new LinkedHashMap();
    }

    @JsonIgnore
    public String getSearchValue() {
        String search = "";
        if (queries != null) {
            if (queries.get("search") == null) {
                return search;
            }
            search = queries.get("search").toString();
        }
        return search;
    }

    public DynatableResponse getDynatableResponse(Object data) {

        DynatableResponse dynatableResponse = new DynatableResponse();
        dynatableResponse.setFiltered(this.filtered);
        dynatableResponse.setTotal(this.total);
        dynatableResponse.setData(data);

        return dynatableResponse;

    }

}
