/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.services.task.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.seam.transaction.Transactional;
import org.jbpm.services.task.impl.model.ContentImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.TaskAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Transactional
@ApplicationScoped
public class TaskAdminServiceImpl implements TaskAdminService {

    private static final Logger logger = LoggerFactory.getLogger(TaskAdminServiceImpl.class);
    
    @Inject
    private JbpmServicesPersistenceManager pm;

    public TaskAdminServiceImpl() {
    }

    public void setPm(JbpmServicesPersistenceManager pm) {
        this.pm = pm;
    }

    public List<TaskSummary> getActiveTasks() {
        HashMap<String, Object> params = pm.addParametersToMap(
                "status", Arrays.asList(Status.InProgress),
                "language", "en-UK");

        return (List<TaskSummary>) pm.queryWithParametersInTransaction("TasksByStatus", params);
    }

    public List<TaskSummary> getActiveTasks(Date since) {
        HashMap<String, Object> params = pm.addParametersToMap(
                "status", Arrays.asList(Status.InProgress),
                "language", "en-UK",
                "since", since);

        return (List<TaskSummary>) pm.queryWithParametersInTransaction("TasksByStatus", params);
    }

    public List<TaskSummary> getCompletedTasks() {
        HashMap<String, Object> params = pm.addParametersToMap(
                "status", Arrays.asList(Status.Completed),
                "language", "en-UK");

        return (List<TaskSummary>) pm.queryWithParametersInTransaction("TasksByStatus", params);
    }

    public List<TaskSummary> getCompletedTasks(Date since) {
        HashMap<String, Object> params = pm.addParametersToMap(
                "status", Arrays.asList(Status.Completed),
                "language", "en-UK",
                "since", since);

        return (List<TaskSummary>) pm.queryWithParametersInTransaction("TasksByStatusSince", params);
    }

    public List<TaskSummary> getCompletedTasksByProcessId(Long processId) {
        HashMap<String, Object> params = pm.addParametersToMap(
                "status", Arrays.asList(Status.Completed),
                "language", "en-UK",
                "processInstanceId", processId);

        return (List<TaskSummary>) pm.queryWithParametersInTransaction("TasksByStatusByProcessId", params);
    }

    public int archiveTasks(List<TaskSummary> tasks) {
        int archivedTasks = 0;
        for (TaskSummary sum : tasks) {
            long taskId = sum.getId();
            TaskImpl task = (TaskImpl) pm.find(TaskImpl.class, taskId);
            task.setArchived(true);
            pm.persist(task);
            archivedTasks++;

        }
        return archivedTasks;
    }

    public List<TaskSummary> getArchivedTasks() {
        HashMap<String, Object> params = pm.addParametersToMap(
                "language", "en-UK");
        return (List<TaskSummary>) pm.queryWithParametersInTransaction("ArchivedTasks", params);
    }

    public int removeTasks(List<TaskSummary> tasks) {
        int removedTasks = 0;

        for (TaskSummary sum : tasks) {
            long taskId = sum.getId();
            // Only remove archived tasks
            TaskImpl task = (TaskImpl) pm.find(TaskImpl.class, taskId);
            ContentImpl content = (ContentImpl) pm.find(ContentImpl.class, task.getTaskData().getDocumentContentId());
            if (task.isArchived()) {
                pm.remove(task);
                if (content != null) {
                    pm.remove(content);
                }
                removedTasks++;
            } else {
                logger.warn(" The Task cannot be removed if it wasn't archived first !!");
            }
        }

        return removedTasks;
    }

    public int removeAllTasks() {
        List<TaskImpl> tasks = (List<TaskImpl>) pm.queryStringInTransaction("select t from TaskImpl t");
        int count = 0;
        for (TaskImpl t : tasks) {
            pm.remove(t);
            count++;
        }
        return count;
    }
}
