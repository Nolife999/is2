package it.istat.is2.workflow.batch;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.istat.is2.app.bean.NotificationMessage;
import it.istat.is2.app.bean.SessionBean;
import it.istat.is2.app.domain.Log;
import it.istat.is2.app.domain.User;
import it.istat.is2.app.service.LogService;
import it.istat.is2.app.service.NotificationService;
import it.istat.is2.app.util.IS2Const;

@RequestMapping("/rest")
@RestController
public class WorkFlowBatchController {

    @Autowired
    JobLauncher jobLauncher;
    @Autowired
    private NotificationService notificationService;

    @Autowired
    Job doBusinessProc;

    @Autowired
    LogService logService;

    @Autowired
    WorkFlowBatchService workFlowBatchService;

    @Autowired
    JobOperator jobOperator;

    @Autowired
    JobRepository jobRepository;

    @RequestMapping(value = "/batch/{idElaborazione}/{idBProc}", method = RequestMethod.GET)
    public List<NotificationMessage> doBatch(HttpSession session, Model model, @AuthenticationPrincipal User user,
            @PathVariable("idElaborazione") Long idElaborazione, @PathVariable("idBProc") Long idBProc)
            throws NoSuchJobException {
    	notificationService.removeAllMessages();
        JobParameters jobParameters = new JobParametersBuilder().addLong("idElaborazione", idElaborazione)
                .addLong("idBProc", idBProc).addLong("time", System.currentTimeMillis()).toJobParameters();
        try {
            jobLauncher.run(doBusinessProc, jobParameters);
        } catch (Exception e) {
            logService.save(e.getMessage());
        }
        
        return notificationService.getNotificationMessages();
    }

    @GetMapping("/batch/logs")
    public ResponseEntity<?> getLogs(HttpSession httpSession, Model model) {
        SessionBean sessionBean = (SessionBean) httpSession.getAttribute(IS2Const.SESSION_BEAN);
        List<Log> logs;
        if (sessionBean != null) {
            logs = logService.findByIdSessione(sessionBean.getId());
        } else {
            logs = new ArrayList<>();
        }
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/batch/jobs")
    public ResponseEntity<?> getJobs(HttpSession httpSession, Model model) {
        SessionBean sessionBean = (SessionBean) httpSession.getAttribute(IS2Const.SESSION_BEAN);
        List<Batch> batches;
        if (sessionBean != null) {
            batches = workFlowBatchService.findByIdSessione(sessionBean.getId());
        } else {
            batches = new ArrayList<>();
        }
        return ResponseEntity.ok(batches);

    }

}
