package com.github.index.schedule.utils;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class OtherUtils {
    private static final Logger LOGGER = Logger.getLogger(OtherUtils.class);

    private OtherUtils() {

    }

    /**
     * @param request   HttpServletRequest с параметрами
     * @param parameter имя параметра
     * @param tClass    ожидаемый тип параметра
     * @return Optional.empty() если параметр не существует или пуст
     */
    public static <T> Optional<T> getParameterIfPresent(HttpServletRequest request, String parameter, Class<T> tClass) {
        parameter = request.getParameter(parameter);
        Optional<T> result;
        if (parameter != null && !parameter.isEmpty()) {
            T type = null;
            try {
                if (tClass.equals(Integer.class)) {
                    type = (T) Integer.valueOf(Integer.parseInt(parameter));
                } else if (tClass.equals(Character.class)) {
                    type = (T) Character.valueOf(parameter.charAt(0));
                } //todo: Для иных типов если понадобится
            } catch (Exception e) {
                LOGGER.warn("Ошибка парсинга параметра", e);
            }
            result = Optional.<T>ofNullable(type);
        } else {
            result = Optional.empty();
        }
        return result;
    }
}
