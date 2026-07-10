package com.ypat.service;

import com.ypat.InternalTestGenerateQo;
import com.ypat.InternalTestResourceQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("SYSTEM-API")
public interface InternalTestServiceClient {

    @GetMapping("/service/internal-test/resources")
    String resources(@RequestParam(value = "mediaType", required = false) String mediaType,
                     @RequestParam(value = "usageType", required = false) String usageType,
                     @RequestParam(value = "styleCode", required = false) String styleCode,
                     @RequestParam(value = "profession", required = false) String profession,
                     @RequestParam(value = "province", required = false) String province,
                     @RequestParam(value = "city", required = false) String city,
                     @RequestParam(value = "area", required = false) String area,
                     @RequestParam(value = "status", required = false) String status,
                     @RequestParam(value = "usedFlag", required = false) Integer usedFlag,
                     @RequestParam(value = "groupNo", required = false) String groupNo,
                     @RequestParam(value = "keyword", required = false) String keyword,
                     @RequestParam("page") Integer page,
                     @RequestParam("size") Integer size);

    @PostMapping("/service/internal-test/resources")
    String saveResource(@RequestBody InternalTestResourceQo qo);

    @PostMapping("/service/internal-test/resources/batch")
    String batchResources(@RequestBody InternalTestResourceQo qo);

    @GetMapping("/service/internal-test/resource-groups")
    String resourceGroups(@RequestParam(value = "mediaType", required = false) String mediaType,
                          @RequestParam(value = "usageType", required = false) String usageType,
                          @RequestParam(value = "styleCode", required = false) String styleCode,
                          @RequestParam(value = "profession", required = false) String profession,
                          @RequestParam(value = "province", required = false) String province,
                          @RequestParam(value = "city", required = false) String city,
                          @RequestParam(value = "area", required = false) String area,
                          @RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "usedFlag", required = false) Integer usedFlag,
                          @RequestParam(value = "groupNo", required = false) String groupNo,
                          @RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(value = "page", required = false) Integer page,
                          @RequestParam(value = "size", required = false) Integer size);

    @PostMapping("/service/internal-test/resources/update")
    String updateResource(@RequestBody InternalTestResourceQo qo);

    @PostMapping("/service/internal-test/resources/status")
    String updateResourceStatus(@RequestParam("id") Long id,
                                @RequestParam("status") String status);

    @GetMapping("/service/internal-test/users")
    String users(@RequestParam(value = "batchNo", required = false) String batchNo,
                 @RequestParam(value = "city", required = false) String city,
                 @RequestParam(value = "area", required = false) String area,
                 @RequestParam(value = "profess", required = false) String profess,
                 @RequestParam(value = "gender", required = false) String gender);

    @PostMapping("/service/internal-test/users/create")
    String createUsers(@RequestBody InternalTestGenerateQo qo);

    @PostMapping("/service/internal-test/generate")
    String generate(@RequestBody InternalTestGenerateQo qo);

    @GetMapping("/service/internal-test/batches")
    String batches(@RequestParam(value = "batchNo", required = false) String batchNo);

    @PostMapping("/service/internal-test/cleanup")
    String cleanup(@RequestBody InternalTestGenerateQo qo);
}
