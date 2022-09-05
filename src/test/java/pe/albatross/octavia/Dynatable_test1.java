package pe.albatross.octavia;

import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import pe.albatross.octavia.beans.Alumno;
import pe.albatross.octavia.dynatable.DynatableFilter;
import pe.albatross.octavia.dynatable.DynatableSql;

@Slf4j
public class Dynatable_test1 {

    @Test
    public void select_join_test_1() {

        DynatableFilter filter = new DynatableFilter();
        filter.setQueries(new HashMap());
        filter.getQueries().put("search", "D010");

        DynatableSql sql = new DynatableSql(filter)
                .from(Alumno.class, "alu")
                .join("persona per", "facultad fac")
                .leftJoin("estadoAcademico ea")
                .searchFields("alu.codigo", "per.paterno", "fac.codigo");

        sql.createQueriesPrevious();

        this.verify_query_count1(sql);
        this.verify_query_filtered1(sql);
        this.verify_query_data1(sql);
    }

    private void verify_query_count1(DynatableSql sql) {
        log.info("totalQuery.sql={}", sql.toString("totalQuery"));

        String sqlx = " select count(*)  \n";
        sqlx += " from Alumno as alu \n";
        sqlx += " inner join alu.persona as per \n";
        sqlx += " inner join alu.facultad as fac \n";
        sqlx += " left join alu.estadoAcademico as ea \n";

        assertEquals(sqlx, sql.toString("totalQuery"));
    }

    private void verify_query_filtered1(DynatableSql sql) {
        log.info("filteredQuery.sql={}", sql.toString("filteredQuery"));

        String sqlx = " select count(*)  \n";
        sqlx += " from Alumno as alu \n";
        sqlx += " inner join alu.persona as per \n";
        sqlx += " inner join alu.facultad as fac \n";
        sqlx += " left join alu.estadoAcademico as ea \n";
        sqlx += " where  ( \n";
        sqlx += "	alu.codigo like :PARAM_000002 \n";
        sqlx += "	 or per.paterno like :PARAM_000003 \n";
        sqlx += "	 or fac.codigo like :PARAM_000004 \n";
        sqlx += ") \n";

        assertEquals(sqlx, sql.toString("filteredQuery"));
    }

    private void verify_query_data1(DynatableSql sql) {
        log.info("dataQuery.sql={}", sql.toString("dataQuery"));

        String sqlx = " from Alumno as alu \n";
        sqlx += " inner join fetch alu.persona as per \n";
        sqlx += " inner join fetch alu.facultad as fac \n";
        sqlx += " left join fetch alu.estadoAcademico as ea \n";
        sqlx += " where  ( \n";
        sqlx += "	alu.codigo like :PARAM_000002 \n";
        sqlx += "	 or per.paterno like :PARAM_000003 \n";
        sqlx += "	 or fac.codigo like :PARAM_000004 \n";
        sqlx += ") \n";

        assertEquals(sqlx, sql.toString("dataQuery"));
    }

    @Test
    public void select_filter_test_1() {

        DynatableFilter filter = new DynatableFilter();
        filter.setQueries(new HashMap());
        filter.getQueries().put("search", "D010");

        DynatableSql sql = new DynatableSql(filter)
                .from(Alumno.class, "alu")
                .join("persona per", "facultad fac", "per.paisNacimiento pan")
                .leftJoin("estadoAcademico ea")
                .filter("pan.codigo", "pe")
                .searchFields("alu.codigo", "per.paterno", "fac.codigo")
                .filter("alu.codigo", "like", "%2022%")
                .beginBlock()
                .__().filter("per.paterno", "Manu")
                .__().filter("per.nombres", "Karen")
                .endBlock();

        sql.createQueriesPrevious();

        log.info("totalQuery.sql={}", sql.toString("totalQuery"));
        log.info("filteredQuery.sql={}", sql.toString("filteredQuery"));
        log.info("dataQuery.sql={}", sql.toString("dataQuery"));

        this.verify_query_count2(sql);
        this.verify_query_filtered2(sql);
        this.verify_query_data2(sql);
    }

    private void verify_query_count2(DynatableSql sql) {
        log.info("totalQuery.sql={}", sql.toString("totalQuery"));

        String sqlx = " select count(*)  \n";
        sqlx += " from Alumno as alu \n";
        sqlx += " inner join alu.persona as per \n";
        sqlx += " inner join alu.facultad as fac \n";
        sqlx += " inner join per.paisNacimiento as pan \n";
        sqlx += " left join alu.estadoAcademico as ea \n";
        sqlx += " where pan.codigo = :PARAM_000001 \n";
        sqlx += " and alu.codigo like :PARAM_000002 \n";
        sqlx += " and  ( \n";
        sqlx += "	per.paterno = :PARAM_000004 \n";
        sqlx += "	 or per.nombres = :PARAM_000005 \n";
        sqlx += ") \n";

        assertEquals(sqlx, sql.toString("totalQuery"));
    }

    private void verify_query_filtered2(DynatableSql sql) {
        log.info("filteredQuery.sql={}", sql.toString("filteredQuery"));

        String sqlx = " select count(*)  \n";
        sqlx += " from Alumno as alu \n";
        sqlx += " inner join alu.persona as per \n";
        sqlx += " inner join alu.facultad as fac \n";
        sqlx += " inner join per.paisNacimiento as pan \n";
        sqlx += " left join alu.estadoAcademico as ea \n";
        sqlx += " where pan.codigo = :PARAM_000001 \n";
        sqlx += " and alu.codigo like :PARAM_000002 \n";
        sqlx += " and  ( \n";
        sqlx += "	per.paterno = :PARAM_000004 \n";
        sqlx += "	 or per.nombres = :PARAM_000005 \n";
        sqlx += ") \n";
        sqlx += " and  ( \n";
        sqlx += "	alu.codigo like :PARAM_000007 \n";
        sqlx += "	 or per.paterno like :PARAM_000008 \n";
        sqlx += "	 or fac.codigo like :PARAM_000009 \n";
        sqlx += ") \n";

        assertEquals(sqlx, sql.toString("filteredQuery"));
    }

    private void verify_query_data2(DynatableSql sql) {
        log.info("dataQuery.sql={}", sql.toString("dataQuery"));

        String sqlx = " from Alumno as alu \n";
        sqlx += " inner join fetch alu.persona as per \n";
        sqlx += " inner join fetch alu.facultad as fac \n";
        sqlx += " inner join fetch per.paisNacimiento as pan \n";
        sqlx += " left join fetch alu.estadoAcademico as ea \n";
        sqlx += " where pan.codigo = :PARAM_000001 \n";
        sqlx += " and alu.codigo like :PARAM_000002 \n";
        sqlx += " and  ( \n";
        sqlx += "	per.paterno = :PARAM_000004 \n";
        sqlx += "	 or per.nombres = :PARAM_000005 \n";
        sqlx += ") \n";
        sqlx += " and  ( \n";
        sqlx += "	alu.codigo like :PARAM_000007 \n";
        sqlx += "	 or per.paterno like :PARAM_000008 \n";
        sqlx += "	 or fac.codigo like :PARAM_000009 \n";
        sqlx += ") \n";

        assertEquals(sqlx, sql.toString("dataQuery"));
    }

}
