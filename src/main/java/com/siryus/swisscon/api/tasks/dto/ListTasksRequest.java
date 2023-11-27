package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * All individual criteria will be combined using `AND` logical operation
 */

@Builder(toBuilder = true)
public class ListTasksRequest {
    /**
     * If empty, location will not be used to filter out task
     * If not empty, only tasks from listed locations will be returned
     */
    @NotNull
    private final List<Integer> locationIDs;

    /**
     * If empty, SNP catalog items will not be used to filter out tasks
     * If not empty, only tasks associated with listed SNP will be returned
     */
    @NotNull
    private final List<String> snpIDs;

    /**
     * If empty, company will not be used to filter out tasks
     * If not empty, only tasks associated with provided companies (via specification) will be returned
     */
    @NotNull
    private final List<Integer> companyIDs;

    /**
     * If empty, (company) trade will not be used to filter out tasks
     * If not empty, only tasks associated with companies which provide specified trades will be returned
     */
    @NotNull
    private final List<Integer> tradeIDs;

    /**
     * If not null, only tasks associated with given user ID will be returned
     */
    private final Integer userID;

    /**
     * If not null, only tasks with given status will be returned
     */
    private final List<TaskStatus> statuses;

    /**
     * If not null, only tasks with startDate and/or dueDate on or after provided value will be returned
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime fromDate;

    /**
     * If not null, only tasks with startDate and/or dueDate on or before provided value will be returned
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime toDate;

    /**
     * if not null, only tasks with task number starting with provided value will be returned
     */
    private final String taskNumber;

    /**
     * If not null, only tasks with title starting with provided value will be returned
     */
    private final String title;

    /**
     * If null > only not disabled tasks will be returned
     */
    private final Boolean isDisabled;

    @JsonCreator
    public ListTasksRequest(
            @JsonProperty("locationIDs") List<Integer> locationIDs,
            @JsonProperty("snpIDs")  List<String> snpIDs,
            @JsonProperty("companyIDs") List<Integer> companyIDs,
            @JsonProperty("tradeIDs") List<Integer> tradeIDs,
            @JsonProperty("userID") Integer userID,
            @JsonProperty("status") TaskStatus status,
            @JsonProperty("statuses") List<TaskStatus> statuses,
            @JsonProperty("fromDate") LocalDateTime fromDate,
            @JsonProperty("toDate") LocalDateTime toDate,
            @JsonProperty("taskNumber") String taskNumber,
            @JsonProperty("title") String title,
            @JsonProperty("isDisabled") Boolean isDisabled
    ) {
        this(
            orEmptyList(locationIDs),
            orEmptyList(snpIDs),
            orEmptyList(companyIDs),
            orEmptyList(tradeIDs),
            userID,
            scalarOrList(status, statuses) ,
            fromDate,
            toDate,
            taskNumber,
            title,
            isDisabled
        );
    }

    public ListTasksRequest(
            List<Integer> locationIDs,
            List<String> snpIDs,
            List<Integer> companyIDs,
            List<Integer> tradeIDs,
            Integer userID,
            List<TaskStatus> statuses,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String taskNumber,
            String title,
            Boolean isDisabled
    ) {
        this.locationIDs = orEmptyList(locationIDs);
        this.snpIDs = orEmptyList(snpIDs);
        this.companyIDs = orEmptyList(companyIDs);
        this.tradeIDs = orEmptyList(tradeIDs);
        this.userID = userID;
        this.statuses = orEmptyList(statuses);
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.taskNumber = taskNumber;
        this.title = title;
        this.isDisabled = isDisabled;
    }

    public List<Integer>  getLocationIDs() {
        return locationIDs;
    }

    public List<String> getSnpIDs() {
        return snpIDs;
    }

    public List<Integer> getCompanyIDs() {
        return companyIDs;
    }

    public List<Integer> getTradeIDs() {
        return tradeIDs;
    }

    public Integer getUserID() {
        return userID;
    }

    public List<TaskStatus> getStatuses() {
        return statuses;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public LocalDateTime getToDate() {
        return toDate;
    }

    public String getTaskNumber() {
        return taskNumber;
    }

    public String getTitle() {
        return title;
    }

    public Boolean getIsDisabled() { return isDisabled; }

    private static <T> List<T> orEmptyList(List<T> listOrNull) {
        return listOrNull == null ? Collections.emptyList() : Collections.unmodifiableList(listOrNull);
    }
    private static List<TaskStatus> scalarOrList(TaskStatus scalar, List<TaskStatus> list) {
        if (list != null) {
            return Collections.unmodifiableList(list);
        }

        if (scalar == null || TaskStatus.ANY.equals(scalar)) {
            return Collections.emptyList();
        }

        return Collections.singletonList(scalar);
    }
}
