package com.vpsoft.provider.ghbankvpm.contract.rest;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.vpsoft.api.common.base.BaseCtrl;
import com.vpsoft.api.common.bean.RestEntity;
import com.vpsoft.api.common.bean.RestList;
import com.vpsoft.api.common.bean.RestMap;
import com.vpsoft.api.common.builder.RestEntityBuilder;
import com.vpsoft.api.common.util.StringUtil;
import com.vpsoft.provider.base.db.util.Page;
import com.vpsoft.provider.base.util.VPUserUtil;
import com.vpsoft.provider.vpm.dynameicForm.service.DynamicFormSerivce;
import com.vpsoft.provider.ghbankvpm.contract.service.VpContractPaymentService;

/**
 * @author shaodh
 * @data 2018年12月24日
 * @version 7.1
 */
@RequestMapping("/contractPayment")
@Controller
public class VpContractPaymentRest extends BaseCtrl {

	@Resource
	private VpContractPaymentService contractservice;
	
	@Resource
	private DynamicFormSerivce formservice;

	@RequestMapping("/page")
	public RestEntity page(HttpServletRequest request) {
		String currentPage = "1";
		String pageSize = "10";

		if (StringUtils.isNotBlank(request.getParameter("currentPage"))) {
			currentPage = request.getParameter("currentPage");
		}
		if (StringUtils.isNotBlank(request.getParameter("pageSize"))) {
			pageSize = request.getParameter("pageSize");
		} else {
			if (StringUtils.isNotBlank(request.getParameter("numPerPage"))) {
				pageSize = request.getParameter("numPerPage");
			}
		}
		String viewtype = request.getParameter("viewtype");
		String contractid = request.getParameter("contractid");

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("currentPage", currentPage);
		param.put("pageSize", pageSize);
		param.put("viewtype", viewtype);
		param.put("contractid", contractid);

		Page opage = contractservice.page(param);
		RestMap map = new RestMap();
		map.put("list", opage);
		map.put("entityrole", VPUserUtil.checkAccessLevel(viewtype));
		return new RestEntityBuilder().data(map).build();
	}
	
	@RequestMapping("/save")
	public RestEntity save(HttpServletRequest request) {
		String param = request.getParameter("sparam");
		String viewtype = request.getParameter("viewtype");
		String iid = request.getParameter("iid");
		String contractid = request.getParameter("contractid");
		if (StringUtil.isEmpty(iid)) {
			iid = "0";
		}
		JSONObject oparam = null;
		if (StringUtil.isNotEmpty(param)) {
			oparam = JSONObject.parseObject(param);
		} else {
			oparam = new JSONObject();
		}
		contractservice.save(Long.parseLong(iid), viewtype, Integer.parseInt(contractid), oparam);
		return new RestEntityBuilder().build();
	}
	
	@RequestMapping("/delete")
	public RestEntity delete(HttpServletRequest request) {
		String viewtype = request.getParameter("viewtype");
		String iids = request.getParameter("iids");
		String contractid = request.getParameter("contractid");
		contractservice.delete(iids, viewtype, Integer.parseInt(contractid));
		return new RestEntityBuilder().build();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@RequestMapping("/getform")
	public RestEntity getform(HttpServletRequest request) throws NumberFormatException, Exception {
		String viewtype = request.getParameter("viewtype");
		String iids = request.getParameter("iids");
		String contractid = request.getParameter("contractid");
		RestList fields = contractservice.getFields(viewtype);
		RestMap dynamicForm = new RestMap();
		if (StringUtils.isBlank(iids)) {
			Map frominfo = new HashMap();
			frominfo.put("isubmitfinance", "未提交");
			dynamicForm = formservice.getdynamicForm(fields, frominfo);
		} else {
			Map frominfo = contractservice.getfrominfo(Integer.parseInt(iids), viewtype);
			String isubmitfinance = "已提交";
			if("1".equals(frominfo.get("isubmitfinance"))) {
				isubmitfinance = "未提交";
			} 
			frominfo.put("isubmitfinance", isubmitfinance);
			dynamicForm = formservice.getdynamicForm(fields, frominfo);
		}

		double famount = contractservice.getContract(contractid);
		DecimalFormat df = new DecimalFormat("#0.0#");
		dynamicForm.put("famount", df.format(famount));
		return new RestEntityBuilder().data(dynamicForm).build();
	}
	
	@RequestMapping("/autoCalculate")
	public RestEntity autoCalculate(HttpServletRequest request) {
		String famount = request.getParameter("famount");
		String contractid = request.getParameter("contractid");
		RestMap res = contractservice.autoCalculate(contractid, famount);
		return new RestEntityBuilder().data(res).build();
	}

}
