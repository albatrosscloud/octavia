package pe.albatross.octavia;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import pe.albatross.octavia.exceptions.OctaviaException;
import pe.albatross.octavia.helpers.Preconditions;

public class Insecto {

    private Class clazz;
    private List<String> attrs;
    private List<String> columns;
    private List values;
    private String tipo;
    private Map<String, String> mapColumns;
    private Map<Long, Long> mapVerificaIds;
    private Map<Integer, Long> mapIds;

    private final String COMMA = ",";
    private final String NULL = "null";
    private final String ATTR_ID = "id";
    private final String QUOTES = "'";
    private final String DATE_FORMAT_STANDARD = "yyyy-MM-dd HH:mm:ss";

    public static final String INSERT_INTO = "INSERT_INTO";
    public static final String UPDATE = "UPDATE";

    public static List<Class> TYPICAL_CLASSES = Arrays.asList(
            String.class, Integer.class, Long.class, BigDecimal.class, Float.class, Double.class, Boolean.class,
            Timestamp.class, Date.class, java.sql.Date.class
    );

    public Insecto(String tipo) {
        this.attrs = new ArrayList();
        this.columns = new ArrayList();
        this.values = new ArrayList();
        this.mapColumns = new LinkedHashMap();
        this.mapVerificaIds = new LinkedHashMap();
        this.mapIds = new LinkedHashMap();
        this.tipo = tipo;
    }

    public static Insecto createInsert() {
        return new Insecto(INSERT_INTO);
    }

    public static Insecto createUpdate(Class clazz) {
        Insecto insecto = new Insecto(UPDATE);
        insecto.setClass(clazz);
        return insecto;
    }

    public Insecto setClass(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public Insecto into(Class clazz) {
        if (tipo.equals(INSERT_INTO)) {
            this.setClass(clazz);
        }
        return this;
    }

    public Insecto with(List values) {
        if (tipo.equals(UPDATE)) {
            this.values.addAll(values);
        }
        return this;
    }

    public Insecto values(List values) {
        if (tipo.equals(INSERT_INTO)) {
            this.values.addAll(values);
        }
        return this;
    }

    public Insecto columns(String... columns) {
        if (tipo.equals(INSERT_INTO)) {
            for (String col : columns) {
                this.attrs.add(col);
            }
        }
        return this;
    }

    public Insecto set(String... columns) {
        if (tipo.equals(UPDATE)) {
            for (String col : columns) {
                this.attrs.add(col);
            }
        }
        return this;
    }

    public String getSql() {
        return toString();
    }

    @Override
    public String toString() {
        if (tipo.equals(INSERT_INTO)) {
            return createSqlToInsert();

        } else if (tipo.equals(UPDATE)) {
            return createSqlToUpdate();

        } else {
            return "";
        }
    }

    private String createSqlToUpdate() {
        int count = 0;
        for (Object row : values) {
            count++;
            Long idRow = getIdObject(row);
            mapIds.put(count, idRow);
            mapVerificaIds.put(idRow, idRow);
        }

        if (mapIds.size() != mapVerificaIds.size()) {
            return "ERROR UPDATE REGISTROS DUPLICADOS...";
        }

        StringBuilder sql = new StringBuilder("update ");
        findTableName(sql);
        sql.append(" set \n");

        findColumnsName();

        List<Long> idsRows = new ArrayList(mapVerificaIds.values());
        int loop = 0;
        for (String attr : attrs) {
            loop++;
            String col = mapColumns.get(attr);
            sql.append(col).append(" = case id \n");

            count = 0;
            for (Object row : values) {
                count++;
                Long idRow = mapIds.get(count);

                sql.append("\t when ");
                sql.append(idRow);
                sql.append(" then ");
                sql.append(getNewValue(row, attr));
                sql.append(" ");
                if (count < values.size()) {
                    sql.append("\n");
                }
            }

            sql.append(" end ");
            if (loop < attrs.size()) {
                sql.append(COMMA);
            }
            sql.append("\n");
        }

        sql.append(" where id in (");

        loop = 0;
        for (Long id : idsRows) {
            loop++;
            sql.append(id);
            if (loop < idsRows.size()) {
                sql.append(COMMA);
            }
        }

        sql.append(")");

        return sql.toString();
    }

    private String createSqlToInsert() {
        StringBuilder sql = new StringBuilder("insert into ");
        findTableName(sql);

        findColumnsName();
        sql.append("(");
        int loop = 0;
        for (String col : columns) {
            loop++;
            sql.append(col);
            if (loop < columns.size()) {
                sql.append(COMMA);
            }
        }
        sql.append(") values \n");

        loop = 0;
        for (Object row : values) {
            loop++;

            int count = 0;
            sql.append("(");
            for (String attr : attrs) {
                count++;
                sql.append(getNewValue(row, attr));
                if (count < attrs.size()) {
                    sql.append(COMMA);
                }
            }
            sql.append(")");
            if (loop < values.size()) {
                sql.append(COMMA);
            }
            sql.append("\n");
        }

        return sql.toString();
    }

    private String getNewValue(Object row, String attr) {
        Object val = getValue(row, attr, null, this.clazz);
        if (val == null) {
            return NULL;
        }

        if (!TYPICAL_CLASSES.contains(val.getClass())) {
            val = getValue(val, ATTR_ID, attr, val.getClass());
            if (val == null) {
                return NULL;
            }
        }

        Class claxx = val.getClass();
        if (claxx == Date.class) {
            return getDateSql((Date) val);
        } else if (claxx == java.sql.Date.class) {
            return getDateSql((Date) val);
        } else if (claxx == Timestamp.class) {
            return getDateSql((Date) val);
        } else if (claxx == Long.class) {
            return getLongSql((Long) val);
        } else if (claxx == BigDecimal.class) {
            return getBigDecimalSql((BigDecimal) val);
        } else if (claxx == Integer.class) {
            return getIntegerSql((Integer) val);
        } else if (claxx == Boolean.class) {
            return getBooleanSql((Boolean) val);
        } else {
            return getStringSql((String) val);
        }

    }

    private static Object getValue(Object obj, String attr, String parentAttr, Class claxx) {
        Object parent = null;
        Method metodo = null;
        String ini = attr.substring(0, 1);
        String methodName = "get".concat(ini.toUpperCase()).concat(attr.substring(1));

        for (Method methodType : obj.getClass().getMethods()) {
            if (methodName.equals(methodType.getName())) {
                metodo = methodType;
                break;
            }
        }
        try {
            parent = metodo.invoke(obj);

        } catch (InvocationTargetException ex) {
            if (parentAttr == null) {
                throw new OctaviaException("El método [[" + methodName + "]] no es genérico");
            } else {
                throw new OctaviaException("El método [[" + parentAttr + "." + methodName + "]] no es genérico");
            }

        } catch (Exception ex) {
            if (parentAttr == null) {
                throw new OctaviaException("No existe el método [[" + methodName + "]] para la clase " + claxx.getName());
            } else {
                throw new OctaviaException("No existe el método [[" + parentAttr + "." + methodName + "]] para la claxx " + claxx.getName());
            }
        }

        return parent;
    }

    private void findColumnsName() {
        for (String attr : attrs) {
            try {
                Field f = clazz.getDeclaredField(attr);
                try {
                    String column = f.getAnnotation(JoinColumn.class).name();
                    this.columns.add(column);
                    this.mapColumns.put(attr, column);
                } catch (Exception e) {
                    try {
                        String column = f.getAnnotation(Column.class).name();
                        this.columns.add(column);
                        this.mapColumns.put(attr, column);
                    } catch (Exception ee) {
                        throw new OctaviaException("El atributo [[" + clazz.getSimpleName() + "." + attr + "]] no esta mapeada como columna de tabla");
                    }
                }
            } catch (OctaviaException ex) {
                throw new OctaviaException(ex.getLocalizedMessage());
            } catch (Exception ex) {
                throw new OctaviaException("La class [[" + clazz.getSimpleName() + "]] no tiene el atríbuto [[" + attr + "]]");
            }
        }
    }

    private void findTableName(StringBuilder sql) {
        try {
            Table t = (Table) clazz.getAnnotation(Table.class);
            String sch = t.schema();
            sch = sch.equals("") ? t.catalog() : sch;
            if (!sch.equals("")) {
                sql.append(sch).append(".");
            }
            sql.append(t.name());
        } catch (Exception e) {
            throw new OctaviaException("La class " + clazz.getSimpleName() + " no esta mapeada como Tabla");
        }
    }

    private String getDateSql(Date fecha) {
        if (fecha == null) {
            return NULL;
        }

        StringBuilder valuesSql = new StringBuilder(QUOTES);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STANDARD, new Locale("es", "ES"));
            valuesSql.append(sdf.format(fecha));
        } catch (Exception e) {
        }

        valuesSql.append(QUOTES);
        return valuesSql.toString();
    }

    private String getStringSql(String string) {
        if (string == null) {
            return NULL;
        }
        StringBuilder valuesSql = new StringBuilder(QUOTES);
        valuesSql.append(string);
        valuesSql.append(QUOTES);
        return valuesSql.toString();
    }

    private String getBigDecimalSql(BigDecimal number) {
        if (number == null) {
            return NULL;
        }
        return number + "";
    }

    private String getLongSql(Long number) {
        if (number == null) {
            return NULL;
        }
        return number + "";
    }

    private String getIntegerSql(Integer number) {
        if (number == null) {
            return NULL;
        }
        return number + "";
    }

    private String getBooleanSql(Boolean isTrue) {
        if (isTrue == null) {
            return NULL;
        }
        return isTrue + "";
    }

    private Long getIdObject(Object object, boolean withValidation) {
        Method getIdMethod = null;
        for (Method method : object.getClass().getMethods()) {
            if (method.getName().equals("getId")) {
                getIdMethod = method;
                break;
            }
        }
        Preconditions.isNotNull(getIdMethod, "No se encontro el atributo ID para el objecto [[" + object + "]]");

        Long id = null;
        try {
            id = (Long) getIdMethod.invoke(object);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        }

        if (withValidation) {
            Preconditions.isNotNull(id, "El valor del atributo ID es null");
        }
        return id;
    }

    private Long getIdObject(Object object) {
        return getIdObject(object, true);
    }

}
