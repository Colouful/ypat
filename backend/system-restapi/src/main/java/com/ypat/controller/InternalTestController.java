package com.ypat.controller;

import com.ypat.InternalTestGenerateQo;
import com.ypat.InternalTestResourceQo;
import com.ypat.InternalTestUserActionQo;
import com.ypat.ResponseApiBody;
import com.ypat.service.InternalTestDataService;
import com.ypat.service.InternalTestResourceService;
import com.ypat.service.InternalTestUserActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 内测数据工厂内部服务 Controller。
 */
@RestController
@RequestMapping("/service/internal-test")
public class InternalTestController {

    @Autowired
    private InternalTestResourceService resourceService;

    @Autowired
    private InternalTestDataService dataService;

    @Autowired
    private InternalTestUserActionService userActionService;

    @GetMapping("/resources")
    public ResponseApiBody resources(InternalTestResourceQo qo) {
        return ResponseApiBody.success(resourceService.page(qo));
    }

    @PostMapping("/resources")
    public ResponseApiBody saveResource(@RequestBody InternalTestResourceQo qo) {
        return ResponseApiBody.success(resourceService.save(qo));
    }

    @PostMapping("/resources/batch")
    public ResponseApiBody batchResources(@RequestBody InternalTestResourceQo qo) {
        return ResponseApiBody.success(resourceService.batchSave(qo));
    }

    @GetMapping("/resource-groups")
    public ResponseApiBody resourceGroups(InternalTestResourceQo qo) {
        return ResponseApiBody.success(resourceService.listAvailableGroups(qo));
    }

    @PostMapping("/resources/update")
    public ResponseApiBody updateResource(@RequestBody InternalTestResourceQo qo) {
        return ResponseApiBody.success(resourceService.save(qo));
    }

    @PostMapping("/resources/status")
    public ResponseApiBody updateResourceStatus(@RequestParam("id") Long id,
                                                @RequestParam("status") String status) {
        resourceService.updateStatus(id, status);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", true);
        return ResponseApiBody.success(result);
    }

    @GetMapping("/users")
    public ResponseApiBody users(InternalTestGenerateQo qo) {
        return ResponseApiBody.success(dataService.listUsers(qo));
    }

    @GetMapping("/users/search")
    public ResponseApiBody searchUsers(InternalTestGenerateQo qo) {
        return ResponseApiBody.success(dataService.searchUsers(qo));
    }

    @PostMapping("/users/create")
    public ResponseApiBody createUsers(@RequestBody InternalTestGenerateQo qo) {
        return ResponseApiBody.success(dataService.createUsers(qo));
    }

    @PostMapping("/users/{userId}/grant-member")
    public ResponseApiBody grantMember(@PathVariable("userId") Long userId,
                                       @RequestBody(required = false) InternalTestUserActionQo qo) {
        qo = userActionQo(userId, qo);
        return ResponseApiBody.success(actionResult(userActionService.grantMember(qo)));
    }

    @PostMapping("/users/{userId}/verify")
    public ResponseApiBody verifyUser(@PathVariable("userId") Long userId,
                                      @RequestBody(required = false) InternalTestUserActionQo qo) {
        qo = userActionQo(userId, qo);
        return ResponseApiBody.success(actionResult(userActionService.verifyUser(qo)));
    }

    @PostMapping("/users/{userId}/deposit-paid")
    public ResponseApiBody depositPaid(@PathVariable("userId") Long userId,
                                       @RequestBody(required = false) InternalTestUserActionQo qo) {
        qo = userActionQo(userId, qo);
        return ResponseApiBody.success(actionResult(userActionService.markDepositPaid(qo)));
    }

    @PostMapping("/generate")
    public ResponseApiBody generate(@RequestBody InternalTestGenerateQo qo) {
        return ResponseApiBody.success(dataService.generate(qo));
    }

    @PostMapping("/generate/users")
    public ResponseApiBody generateUsers(@RequestBody InternalTestGenerateQo qo) {
        return ResponseApiBody.success(dataService.generateUsers(qo));
    }

    @PostMapping("/generate/works")
    public ResponseApiBody generateWorks(@RequestBody InternalTestGenerateQo qo) {
        return ResponseApiBody.success(dataService.generateWorks(qo));
    }

    @PostMapping("/generate/ypats")
    public ResponseApiBody generateYpats(@RequestBody InternalTestGenerateQo qo) {
        return ResponseApiBody.success(dataService.generateYpats(qo));
    }

    @GetMapping("/batches")
    public ResponseApiBody batches(InternalTestGenerateQo qo) {
        return ResponseApiBody.success(dataService.listBatches(qo));
    }

    @PostMapping("/cleanup")
    public ResponseApiBody cleanup(@RequestBody InternalTestGenerateQo qo) {
        return ResponseApiBody.success(dataService.cleanup(qo));
    }

    private InternalTestUserActionQo userActionQo(Long userId, InternalTestUserActionQo qo) {
        if (qo == null) {
            qo = new InternalTestUserActionQo();
        }
        qo.setUserId(userId);
        return qo;
    }

    private Map<String, Object> actionResult(boolean success) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", success);
        return result;
    }
}
