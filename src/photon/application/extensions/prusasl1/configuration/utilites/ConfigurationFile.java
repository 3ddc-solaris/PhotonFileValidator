package photon.application.extensions.prusasl1.configuration.utilites;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

abstract public class ConfigurationFile extends Properties {

    public void load(final File file) throws IllegalAccessException, IOException, NoSuchMethodException, InvocationTargetException {
        try (final InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            loadFromXML(in);
        }

        for (final Map.Entry<String, Field> entry : getPersistentFileds().entrySet()) {
            final String name = entry.getKey();
            if(!containsKey(name))
                continue;

            final Field field = entry.getValue();
            final Object defaultValue = field.get(this);
            final String userValue = getProperty(name);

            final Class<?> type = null == defaultValue ? field.getType() // declaration type
                : defaultValue.getClass(); // runtime type

            if (isEnum(type)) {
                if (type.isEnum()) {
                    // simple enum
                    field.set(this, Enum.valueOf((Class<Enum>) type, userValue.toString()));
                } else {
                    // complex enum
                    final Method valueOf = type.getMethod("valueOf", new Class<?>[] { String.class });
                    field.set(this, valueOf.invoke(type, userValue));
                }
                continue;
            }

            if(type.isAssignableFrom(String.class)) {
                field.set(this, userValue);
                continue;
            }

            if(type.isAssignableFrom(Integer.class)) {
                field.set(this, Integer.parseInt(userValue));
                continue;
            }

            if(type.isAssignableFrom(Double.class)) {
                field.set(this, Double.parseDouble(userValue));
                continue;
            }

            if(type.isAssignableFrom(Float.class)) {
                field.set(this, Float.parseFloat(userValue));
                continue;
            }

            if(type.isAssignableFrom(Boolean.class)) {
                field.set(this, Boolean.parseBoolean(userValue));
                continue;
            }

            throw new IllegalArgumentException("Unable to read field " + name + " of class " + getClass().getCanonicalName() + ". Unsupported field type: " + type.getCanonicalName());
        }
    }

    public void save(final File file) throws IllegalAccessException, IOException {
        for (final Map.Entry<String, Field> entry : getPersistentFileds().entrySet()) {
            final String name = entry.getKey();
            final Object value = entry.getValue().get(this);

            setProperty(name, null == value ? null : value.toString());
        }

        try (final OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            storeToXML(out, null);
        }
    }

    private static boolean isEnum(final Class<?> type) {
        return type.isEnum() || java.lang.Enum.class.isAssignableFrom(type);
    }

    private Map<String,Field> getPersistentFileds() {
        final Map<String, Field> fields = new HashMap<>();

        final Persistent a =  getClass().getAnnotation(Persistent.class);
        String prefix = null == a ? null : a.value();
        if(null == prefix || prefix.trim().isEmpty())
            prefix = getClass().getCanonicalName();

        for(final Field field : getFileds(getClass())) {
            if(!field.isAnnotationPresent(Persistent.class))
                continue;

            String label = field.getAnnotation(Persistent.class).value();
            if(null == label || label.trim().isEmpty())
                label = prefix + "." + field.getName();

            fields.put(label, field);
        }

        return fields;
    }

    private static ArrayList<Field> getFileds(final Class<?> cls) {
        final ArrayList<Field> fields = new ArrayList<Field>();

        // collect fields from base class
        for (Class<?> clazz = cls.getSuperclass(); null != clazz; clazz = clazz.getSuperclass()) {
            fields.addAll(getFileds(clazz));
        }

        // collect fields from this
        for (final Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(Persistent.class)) {
                field.setAccessible(true);
                fields.add(field);
            }
        }

        return fields;
    }
}
