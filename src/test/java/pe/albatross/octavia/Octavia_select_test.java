package pe.albatross.octavia;

import lombok.extern.slf4j.Slf4j;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import pe.albatross.octavia.beans.Alumno;

@Slf4j
public class Octavia_select_test {

    @Test
    public void select_join_test_1() {
        Octavia sql = Octavia.query()
                .from(Alumno.class, "alu")
                .join("persona per", "facultad fac")
                .leftJoin("estadoAcademico ea");

        log.info("sql={}", sql.toString());
        String sqlx = " from Alumno as alu \n";
        sqlx += " inner join fetch alu.persona as per \n";
        sqlx += " inner join fetch alu.facultad as fac \n";
        sqlx += " left join fetch alu.estadoAcademico as ea \n";

        assertEquals(sqlx, sql.toString());
    }

    @Test
    public void select_filter_test_1() {
        Octavia sql = Octavia.query()
                .from(Alumno.class, "alu")
                .join("persona per", "facultad fac")
                .leftJoin("estadoAcademico ea")
                .filter("fac.codigo", "AGRO")
                .filter("ea.codigo", "MAT");

        log.info("sql={}", sql.toString());
        String sqlx = " from Alumno as alu \n";
        sqlx += " inner join fetch alu.persona as per \n";
        sqlx += " inner join fetch alu.facultad as fac \n";
        sqlx += " left join fetch alu.estadoAcademico as ea \n";
        sqlx += " where fac.codigo = :PARAM_000001 \n";
        sqlx += " and ea.codigo = :PARAM_000002 \n";

        assertEquals(sqlx, sql.toString());
    }

    @Test
    public void select_filter_block_test_1() {
        Octavia sql = Octavia.query()
                .from(Alumno.class, "alu")
                .join("persona per", "facultad fac", "per.paisNacimiento pan")
                .leftJoin("estadoAcademico ea")
                .beginBlock()
                .__().filter("fac.codigo", "AGRO")
                .__().filter("ea.codigo", "MAT")
                .__().filter("pan.codigo", "pe")
                .endBlock();

        log.info("sql={}", sql.toString());
        String sqlx = " from Alumno as alu \n";
        sqlx += " inner join fetch alu.persona as per \n";
        sqlx += " inner join fetch alu.facultad as fac \n";
        sqlx += " inner join fetch per.paisNacimiento as pan \n";
        sqlx += " left join fetch alu.estadoAcademico as ea \n";
        sqlx += " where  ( \n";
        sqlx += "	fac.codigo = :PARAM_000002 \n";
        sqlx += "	 or ea.codigo = :PARAM_000003 \n";
        sqlx += "	 or pan.codigo = :PARAM_000004 \n";
        sqlx += ") \n";

        assertEquals(sqlx, sql.toString());
    }

}
