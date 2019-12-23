package com.vpsoft.provider.ghbankvpm.contract.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.vpsoft.api.common.base.BaseService;
import com.vpsoft.api.common.bean.RestList;
import com.vpsoft.api.common.bean.RestMap;
import com.vpsoft.api.common.util.StringUtil;
import com.vpsoft.provider.base.db.dao.IDBManager;
import com.vpsoft.provider.base.db.util.FieldValueUtil;
import com.vpsoft.provider.base.db.util.IDGenerator;
import com.vpsoft.provider.base.db.util.Page;
import com.vpsoft.provider.base.db.util.VPEntity;
import com.vpsoft.provider.base.util.VPUserUtil;

/**
 * @author shaodh
 * @data 2018年12月24日
 * @version 7.1
 */

@Service
public class VpContractPaymentService extends BaseService {

	@Resource
	private IDBManager dbm;

	/**
	 * 获取首付款计划，实际，开票信息
	 * 
	 * @param param viewtype：plan （计划） actual（实际）invoice（发票）
	 * @return
	 */
	public Page page(Map<String, Object> param) {
		Page queryPageLowerCase = null;
		String viewtype = (String) param.get("viewtype");

		String contractid = (String) param.get("contractid");

		String currentPage = (String) param.get("currentPage");
		String pageSize = (String) param.get("pageSize");
		StringBuffer sb = new StringBuffer("");

		if ("plan".equals(viewtype)) {
			sb.append(" SELECT iid,icontractid,sname,sdescription,to_char(dplandate,'yyyy-MM-dd') dplandate,to_char(iplannedamount, 'fm99,999,999,999,990.90') iplannedamount,iplanpercent,ssettlementway,(select sname from vpm_contract where iid=a.icontractid) icontractid_name ");
			sb.append(",sincomeconditions,sremark,imodifyuser,dmodifydate");
			sb.append(",(select sname from org_user where iid=a.imodifyuser) imodifyuser_name");
			sb.append(" from vpm_contractplan a where icontractid=" + contractid + " order by sname");
		} else if ("actual".equals(viewtype)) {
			sb.append(" SELECT iid,icontractid,sname,sremark,to_char(dactualdate,'yyyy-MM-dd') dactualdate,to_char(iactualamount, 'fm99,999,999,999,990.90') iactualamount,iactualpercent,imodifyuser,dmodifydate,sdescription,(select sname from vpm_contract where iid=a.icontractid) icontractid_name ");
			sb.append(",(select sname from org_user where iid=a.imodifyuser) imodifyuser_name");
			sb.append(" from vpm_contractactual a where icontractid = " + contractid + " order by sname");
		} else {
			sb.append(" SELECT iid,icontractid,sname,spaymentname,spaymentnum,sreceivablesname,sreceivablesnum,to_char(iinvoicevalue, 'fm99,999,999,999,990.90') iinvoicevalue,(select sname from vpm_contract where iid=a.icontractid) icontractid_name ");
			sb.append(",to_char(dinvoicedate,'yyyy-MM-dd') dinvoicedate,iinvoiceuser,sremark,imodifyuser,dmodifydate,sdescription");
			sb.append(",(select sname from org_user where iid=a.imodifyuser) imodifyuser_name");
			sb.append(",(select sname from org_user where iid=a.iinvoiceuser) iinvoiceuser_name");
			sb.append(" from vpm_contractinvoicemanage a where icontractid = " + contractid + " order by sname");
		}

		queryPageLowerCase = this.dbm.queryPageLowerCase(sb.toString(), Integer.parseInt(currentPage), Integer.parseInt(pageSize));

		return queryPageLowerCase;
	}
	/**
	 * 保存表单信息
	 * 
	 * @param iid
	 * @param viewtype
	 * @param contractid
	 * @param oparam
	 * @return
	 */
	public int save(long iid, String viewtype, int contractid,
			JSONObject oparam) {
		IDGenerator idg = new IDGenerator();
		StringBuffer sql = new StringBuffer();
		if (iid == 0) {
			iid = idg.getNewID(dbm, "contractplanid");
			if ("plan".equals(viewtype)) {
				sql.append("insert into vpm_contractplan(iid,icontractid,sname,sdescription,dplandate,iplannedamount,iplanpercent,ssettlementway,sincomeconditions,sremark,imodifyuser,dmodifydate) values (");
				sql.append(iid).append(",").append(contractid).append(",");
				sql.append("'").append(oparam.get("sname")).append("',");
				sql.append("'").append(oparam.get("sdescription"));
				sql.append("',");
				if (StringUtils.isBlank(oparam.getString("dplandate"))) {
					sql.append("'',");
				} else {
					sql.append("to_date('" + oparam.get("dplandate").toString().substring(0, 10)+ "','yyyy-MM-dd'),");
				}
				sql.append("'").append(oparam.get("iplannedamount").toString());
				sql.append("',").append("'");
				sql.append(oparam.get("iplanpercent").toString());
				sql.append("',").append("'");
				sql.append(oparam.get("ssettlementway")).append("',");
				sql.append("'").append(oparam.get("sincomeconditions"));
				sql.append("',").append("'").append(oparam.get("sremark"));
				sql.append("',").append(VPUserUtil.getUserId());
				sql.append(",sysdate)");

			} else if ("actual".equals(viewtype)) {
				sql.append("insert into vpm_contractactual");
				sql.append("(iid,icontractid,sname,sremark,dactualdate,iactualamount,iactualpercent,sdescription,dsubmitfinancedate,isubmitfinance,imodifyuser,dmodifydate) ");
				sql.append("values (");
				sql.append(iid).append(",").append(contractid).append(",");
				sql.append("'").append(oparam.get("sname")).append("',");
				sql.append("'").append(oparam.get("sremark")).append("',");
				if (StringUtils.isBlank(oparam.getString("dactualdate"))) {
					sql.append("''").append(",");
				} else {
					sql.append("to_date('" + oparam.get("dactualdate").toString().substring(0, 10) + "','yyyy-MM-dd'),");
				}
				sql.append("'").append(oparam.get("iactualamount").toString());
				sql.append("',").append("'");
				sql.append(oparam.get("iactualpercent").toString());
				sql.append("',").append("'");
				sql.append(oparam.get("sdescription")).append("',");
				if (StringUtils.isBlank(oparam.getString("dsubmitfinancedate"))) {
					sql.append("''").append(",1,");
				} else {
					sql.append("to_date('" + oparam.get("dsubmitfinancedate").toString().substring(0, 10) + "','yyyy-MM-dd HH24:MI:ss'),0,");
				}
				sql.append(VPUserUtil.getUserId()).append(",sysdate)");
			} else {
				sql.append("insert into vpm_contractinvoicemanage(iid,icontractid,sname,spaymentname,spaymentnum,sreceivablesname,sreceivablesnum,iinvoicevalue,dinvoicedate,iinvoiceuser,sremark,imodifyuser,dmodifydate,sdescription) values (");
				sql.append(iid).append(",").append(contractid).append(",");
				sql.append("'").append(oparam.get("sname")).append("',");
				sql.append("'").append(oparam.get("spaymentname"));
				sql.append("',").append("'");
				sql.append(oparam.get("spaymentnum")).append("',");
				sql.append("'").append(oparam.get("sreceivablesname"));
				sql.append("',").append("'");
				sql.append(oparam.get("sreceivablesnum")).append("',");
				sql.append("'").append(oparam.get("iinvoicevalue"));
				sql.append("',");
				if (StringUtils.isBlank(oparam.getString("dinvoicedate"))) {
					sql.append("''").append(",");
				} else {
					sql.append("to_date('" + oparam.get("dinvoicedate").toString().substring(0, 10) + "','yyyy-MM-dd'),");
				}

				sql.append("'").append(oparam.get("iinvoiceuser").toString()).append("',");
				sql.append("'").append(oparam.get("sremark")).append("',");
				sql.append(VPUserUtil.getUserId()).append(",sysdate");
				sql.append(",'").append(oparam.get("sdescription"));
				sql.append("')");
			}
		} else {
			if ("plan".equals(viewtype)) {
				sql.append("update vpm_contractplan set ");
			} else if ("actual".equals(viewtype)) {
				sql.append("update vpm_contractactual set ");
				sql.append("	   dsubmitfinancedate = to_date('"+oparam.get("dsubmitfinancedate")+"','yyyy-MM-dd HH24:MI:ss'),");
			} else {
				sql.append("update vpm_contractinvoicemanage set ");
			}
			String isubmitfinance = "0";
			if("未提交".equals(oparam.get("isubmitfinance"))) {
				isubmitfinance = "1";
			} 
			oparam.put("isubmitfinance", isubmitfinance);
			oparam.remove("dsubmitfinancedate");
			sql.append(FieldValueUtil.getUpdateFieldValue(oparam));
			
			sql.append(" where iid=" + iid);
		}

		this.dbm.update(sql.toString());
		
		this.updateContractData(contractid);
		return (int) iid;
	}
	/**
	 * 删除对象
	 * 
	 * @param iids
	 * @param viewtype
	 * @param contractid
	 */
	public void delete(String iids, String viewtype, int contractid) {
		StringBuffer sql = new StringBuffer();
		if (!StringUtil.isEmpty(iids)) {
			if ("plan".equals(viewtype)) {
				sql.append("delete vpm_contractplan where iid in (" + iids + ")");
			} else if ("actual".equals(viewtype)) {
				sql.append("delete vpm_contractactual where iid in (" + iids + " )");
			} else {
				sql.append("delete vpm_contractinvoicemanage where iid in (" + iids + " )");
			}
		}
		dbm.delete(sql.toString());
		this.updateContractData(contractid);
	}
	/**
	 * 获取表单信息
	 * 
	 * @param iid
	 * @param viewtype
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public Map getfrominfo(int iid, String viewtype) throws Exception {

		String table = "";

		if ("plan".equals(viewtype)) {
			table = "vpm_contractplan";
		} else if ("actual".equals(viewtype)) {
			table = "vpm_contractactual";
		} else {
			table = "vpm_contractinvoicemanage";
		}

		StringBuffer sqlBody = new StringBuffer();
		sqlBody.append(" SELECT a.*,(select sname from vpm_contract where iid=a.icontractid) icontractid_name ");
		sqlBody.append(",(select sname from org_user where iid=a.imodifyuser) imodifyuser_name");
		if ("invoice".equals(viewtype)) {
			sqlBody.append(",(select sname from org_user where iid=a.iinvoiceuser) iinvoiceuser_name");
		} 
		sqlBody.append(" FROM " + table + " a ");
		sqlBody.append(" WHERE  a.iid = " + iid);
		Map<String, String> mp = this.dbm.queryMapLowerCase(sqlBody.toString());
		return mp;
	}

	public double getContract(String contractiid) {
		return dbm.queryDouble("select famount from vpm_contract where iid=" + contractiid);
	}
	
	public RestList getFields(String viewtype) {
		if ("actual".equals(viewtype)) {
			return getActualFields();
		} else if ("invoice".equals(viewtype)) {
			return getInvoiceFields();
		} else {
			return getPlanFields();
		}
	}

	public static RestList getPlanFields() {
		RestList fields = new RestList();
		RestMap field = new RestMap();
		field.put("field_name", "sname");
		field.put("field_label", "名称");
		field.put("widget_type", "1");
		field.put("all_line", 2);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "sdescription");
		field.put("field_label", "描述");
		field.put("widget_type", "1");
		field.put("all_line", 2);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "dplandate");
		field.put("field_label", "计划日期");
		field.put("widget_type", "17");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "iplannedamount");
		field.put("field_label", "计划金额（元）");
		field.put("widget_type", "4");
		field.put("eventtype", "onchange");
		field.put("seventinfo", "setplanpercent");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "iplanpercent");
		field.put("field_label", "计划比例（%）");
		field.put("disabled", true);
		field.put("widget_type", "1");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "ssettlementway");
		field.put("field_label", "结算方式");
		field.put("widget_type", "1");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "sincomeconditions");
		field.put("field_label", "收款条件");
		field.put("widget_type", "2");
		field.put("all_line", 2);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "sremark");
		field.put("field_label", "备注");
		field.put("widget_type", "2");
		field.put("all_line", 2);
		fields.add(field);
		return fields;
	}

	public static RestList getInvoiceFields() {
		RestList fields = new RestList();
		RestMap field = new RestMap();
		field.put("field_name", "sname");
		field.put("field_label", "发票号");
		field.put("widget_type", "1");
		field.put("all_line", 2);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "sdescription");
		field.put("field_label", "描述");
		field.put("widget_type", "1");
		field.put("all_line", 2);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "spaymentname");
		field.put("field_label", "付款方名称");
		field.put("widget_type", "1");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "spaymentnum");
		field.put("field_label", "付款方经办人");
		field.put("widget_type", "1");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "sreceivablesname");
		field.put("field_label", "收款方名称");
		field.put("widget_type", "1");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "sreceivablesnum");
		field.put("field_label", "收款方经办人");
		field.put("widget_type", "1");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "iinvoicevalue");
		field.put("field_label", "发票金额（元）");
		field.put("widget_type", "4");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "dinvoicedate");
		field.put("field_label", "开票日期");
		field.put("widget_type", "17");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "iinvoiceuser");
		field.put("field_label", "开票人");
		field.put("widget_type", "13");
		field.put("all_line", 2);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "sremark");
		field.put("field_label", "备注");
		field.put("widget_type", "2");
		field.put("all_line", 2);
		fields.add(field);
		return fields;
	}

	public static RestList getActualFields() {
		RestList fields = new RestList();
		RestMap field = new RestMap();
		field.put("field_name", "sname");
		field.put("field_label", "名称");
		field.put("widget_type", "1");
		field.put("all_line", 2);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "sdescription");
		field.put("field_label", "描述");
		field.put("widget_type", "1");
		field.put("all_line", 2);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "dactualdate");
		field.put("field_label", "实际日期");
		field.put("widget_type", "17");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "iactualamount");
		field.put("field_label", "实际金额（元）");
		field.put("widget_type", "4");
		field.put("eventtype", "onchange");
		field.put("seventinfo", "setactualpercent");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "iactualpercent");
		field.put("field_label", "实际比例（%）");
		field.put("disabled", true);
		field.put("widget_type", "1");
		field.put("all_line", 2);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "sremark");
		field.put("field_label", "备注");
		field.put("widget_type", "2");
		field.put("all_line", 2);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "dsubmitfinancedate");
		field.put("field_label", "提交财务时间");
		field.put("widget_type", "17");//6:带时分秒
		field.put("eventtype", "onchange");
		field.put("seventinfo", "setIsSubmit");
		field.put("all_line", 1);
		fields.add(field);
		field = new RestMap();
		field.put("field_name", "isubmitfinance");
		field.put("field_label", "是否提交财务");
		field.put("widget_type", "1");
		field.put("all_line", 1);
		field.put("disabled", true);
		fields.add(field);
		return fields;
	}
	
	private void updateContractData(int contractid) {
		StringBuffer sql = new StringBuffer();
		sql.setLength(0);
		sql.append("select famount from vpm_contract where iid=").append(contractid);
		String famount = dbm.queryString(sql.toString());
		if(StringUtils.isNotBlank(famount) && Float.parseFloat(famount) > 0) { 
			RestMap res = this.autoCalculate(String.valueOf(contractid), famount);
			List<String> sqlList = new ArrayList<String>();
			sql.setLength(0);
			sql.append("update vpm_contract ");
			sql.append("   set fplanamount=").append(res.get("fplanamount").toString().replaceAll(",", "")).append(",");
			sql.append("   	   factualamount=").append(res.get("factualamount").toString().replaceAll(",", "")).append(",");
			sql.append("   	   finvoicedamount=").append(res.get("finvoicedamount").toString().replaceAll(",", ""));
			sql.append(" where iid=").append(contractid);
			sqlList.add(sql.toString());
			sql.setLength(0);
			sql.append("update vpm_contract"+VPEntity.EXTEND+" ");
			sql.append("   set fwsfkx=").append(res.get("fwsfkx").toString().replaceAll(",", "")).append(",");
			sql.append("   	   fthisyearpay=").append(res.get("fthisyearpay").toString().replaceAll(",", "")).append(",");
			sql.append("   	   fthisyearnopay=").append(res.get("fthisyearnopay").toString().replaceAll(",", "")).append(",");
			sql.append("   	   sjd='").append(res.get("sjd")).append("' ");
			sql.append(" where iitemid=").append(contractid);
			sqlList.add(sql.toString());
			dbm.batchUpdate(sqlList);
		}
	}
	public RestMap autoCalculate(String contractid, String famount) {
		RestMap resMap = new RestMap();
		float fplanamount = 0;
		float factualamount = 0;
		float finvoicedamount = 0;
		float fwsfkx = 0;
		String sjd = "";
		float fthisyearpay = 0;
		String sql = " select sum(iplannedamount) from vpm_contractplan where icontractid = " + contractid;
		fplanamount = dbm.queryFloat(sql);
		if(fplanamount < 0) {
			fplanamount = 0;
		}
		sql = " select sum(iactualamount) from vpm_contractactual where icontractid = " + contractid + " and isubmitfinance=0";
		factualamount = dbm.queryFloat(sql);
		if(factualamount < 0) {
			factualamount = 0;
		}
		sql = " select sum(iinvoicevalue) from vpm_contractinvoicemanage where icontractid = " + contractid;
		finvoicedamount = dbm.queryFloat(sql);
		if(finvoicedamount < 0) {
			finvoicedamount = 0;
		}
		fwsfkx = Float.parseFloat(famount) - factualamount;
		if(fwsfkx < 0) {
			fwsfkx = 0;
		}
		sql = " select sum(iactualamount) from vpm_contractactual "
			+ "  where icontractid = " + contractid + " and isubmitfinance=0"
			+ "    and to_char(dsubmitfinancedate,'yyyy') = to_char(sysdate,'yyyy')";
		fthisyearpay = dbm.queryFloat(sql);
		if(fthisyearpay < 0) {
			fthisyearpay = 0;
		}
		sjd = new DecimalFormat(",###.##").format(factualamount/Float.parseFloat(famount)*100) + "%";
		resMap.put("fplanamount", new DecimalFormat(",###.##").format(fplanamount));
		resMap.put("factualamount", new DecimalFormat(",###.##").format(factualamount));
		resMap.put("finvoicedamount", new DecimalFormat(",###.##").format(finvoicedamount));
		resMap.put("fwsfkx", new DecimalFormat(",###.##").format(fwsfkx));
		resMap.put("sjd", sjd);
		resMap.put("fthisyearpay", new DecimalFormat(",###.##").format(fthisyearpay));
		resMap.put("fthisyearnopay", new DecimalFormat(",###.##").format(fwsfkx));
		return resMap;
	}

}
