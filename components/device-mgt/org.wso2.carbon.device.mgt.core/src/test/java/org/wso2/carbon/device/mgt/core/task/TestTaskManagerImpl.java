package org.wso2.carbon.device.mgt.core.task;

import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class TestTaskManagerImpl implements TaskManager {
    private List<TaskInfo> registeredTasks;

    public TestTaskManagerImpl() {
        this.registeredTasks = new ArrayList<>();
    }

    @Override
    public void initStartupTasks() throws TaskException {

    }

    @Override
    public void scheduleTask(String taskName) throws TaskException {

    }

    @Override
    public void rescheduleTask(String taskName) throws TaskException {

    }

    @Override
    public boolean deleteTask(String taskName) throws TaskException {
        if(this.registeredTasks.size() <= 0) {
            throw new TaskException("Cannot delete task.", TaskException.Code.NO_TASK_EXISTS);
        }
        for (TaskInfo task : this.registeredTasks) {
            if (task.getName().contains(taskName)) {
                this.registeredTasks.remove(task);
                return true;
            }
        }
        return false;
    }

    @Override
    public void pauseTask(String taskName) throws TaskException {

    }

    @Override
    public void resumeTask(String taskName) throws TaskException {

    }

    @Override
    public void registerTask(TaskInfo taskInfo) throws TaskException {
        this.registeredTasks.add(taskInfo);
    }

    @Override
    public TaskState getTaskState(String taskName) throws TaskException {
        return null;
    }

    @Override
    public TaskInfo getTask(String taskName) throws TaskException {
        for (TaskInfo task : this.registeredTasks) {
            if (task.getName().contains(taskName)) {
                return task;
            }
        }
        return null;
    }

    @Override
    public List<TaskInfo> getAllTasks() throws TaskException {
        return this.registeredTasks;
    }

    @Override
    public boolean isTaskScheduled(String taskName) throws TaskException {
        return this.registeredTasks.size() > 0;
    }
}
