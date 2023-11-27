package com.siryus.swisscon.api.base;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class PageableUtil {

    public static Pageable buildPageable(Integer page, Integer size, String sort) {
        Assert.isTrue(page >= 0, "Page index must be greater than, or equal to, 0");


        Sort sortObject = PageableUtil.buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObject);

        return pageable;
    }

    /**
     * Handles a <code>sort</code> parameter value as defined in JSON API
     * @param sort
     * @return
     *
     * @see <a href="http://jsonapi.org/format/upcoming/#fetching-sorting">JSON API 1.x, Paging and Sorting</a>
     */
    public static Sort buildSort(String sort) {
        Sort pageableSort = null;
        if (StringUtils.isNotBlank(sort)) {
            String[] sortProps = sort.split(",");
            if (ArrayUtils.isNotEmpty(sortProps)) {
                List<Order> orders = new ArrayList<>(sortProps.length);
                for (String prop : sortProps) {
                    if (prop.startsWith("-")) {
                        orders.add(new Order(Sort.Direction.DESC, prop.substring(1)));
                    }
                    else {
                        orders.add(new Order(Sort.Direction.ASC, prop));
                    }

                }
                pageableSort = Sort.by(orders);
            }
        }
        return pageableSort;
    }

    public static Sort buildSort(String sort, String direction) {
        Sort pageableSort = null;
        if (sort != null && direction != null) {
            List<Order> orders = null;
            Order order = new Order(
                    direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC
                            : Sort.Direction.DESC, sort);
            orders = new ArrayList<Order>(1);
            orders.add(order);
            pageableSort = Sort.by(orders);
        }
        return pageableSort;
    }
}