package com.siryus.swisscon.api.tasks.repos;

import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class MainTaskSpecification {

    public static Specification<MainTaskEntity> withIDs(List<Integer> iDs) {
        return (root, query, criteriaBuilder) ->
                root.get("id").in(iDs.toArray());
    }

    public static Specification<MainTaskEntity> withProject(Integer projectId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("projectId"), projectId);
    }

    public static Specification<MainTaskEntity> withStatuses(List<TaskStatus> statuses) {
        return  statuses == null || statuses.isEmpty() ? null : (root, query, criteriaBuilder) ->
                root.get("status").in(statuses);
    }

    public static Specification<MainTaskEntity> afterDate(LocalDateTime fromDate) {
        return fromDate == null ? null : (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), fromDate),
                criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), fromDate)
        );
    }

    public static Specification<MainTaskEntity> beforeDate(LocalDateTime dueDate) {
        return dueDate == null ? null : (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), dueDate),
                criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), dueDate)
        );
    }

    public static Specification<MainTaskEntity> withLocations(List<Integer> locationIDs) {
        return locationIDs.isEmpty() ? null : (root, query, criteriaBuilder) ->
                root.get("location").get("id").in(locationIDs.toArray())
                ;
    }

    public static Specification<MainTaskEntity> withCompanies(List<Integer> companyIDs) {
        return companyIDs.isEmpty() ? null : (root, query, criteriaBuilder) ->
                root.get("company").get("id").in(companyIDs.toArray())
                ;
    }

    public static Specification<MainTaskEntity> withCatalogItems(List<String> catalogItemIDs) {
        return catalogItemIDs.isEmpty() ? null : (root, query, criteriaBuilder) ->
                root.get("catalogItem").get("id").in(catalogItemIDs.toArray())
                ;
    }

    public static Specification<MainTaskEntity> taskNumberStarts(String taskNumber) {
        return taskNumber == null ? null : (root, query, cb) ->
                cb.like(root.get("taskNumber").as(String.class), taskNumber + "%")
        ;
    }

    public static Specification<MainTaskEntity> titleStarts(String title) {
        return title == null ? null : (root, query, cb) ->
                cb.like( root.get("title"), title + "%")
        ;
    }

    public static Specification<MainTaskEntity> disabled(Boolean isDisabled) {
        return (root, query, criteriaBuilder) -> isDisabled != null && isDisabled ?
                root.get("disabled").isNotNull() : root.get("disabled").isNull();
    }
}
