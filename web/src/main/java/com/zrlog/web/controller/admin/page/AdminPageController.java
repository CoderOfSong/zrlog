package com.zrlog.web.controller.admin.page;

import com.jfinal.core.JFinal;
import com.zrlog.common.Constants;
import com.zrlog.common.response.CheckVersionResponse;
import com.zrlog.model.Comment;
import com.zrlog.model.Log;
import com.zrlog.service.AdminTokenService;
import com.zrlog.service.AdminTokenThreadLocal;
import com.zrlog.util.ZrLogUtil;
import com.zrlog.web.config.ZrLogConfig;
import com.zrlog.web.controller.BaseController;
import com.zrlog.web.controller.admin.api.UpgradeController;

import javax.servlet.http.Cookie;
import java.util.List;

public class AdminPageController extends BaseController {

    private AdminTokenService adminTokenService = new AdminTokenService();

    public String index() {
        getRequest().setAttribute("previewDb", com.zrlog.web.config.ZrLogConfig.isPreviewDb());
        if (AdminTokenThreadLocal.getUser() != null) {
            CheckVersionResponse response = new UpgradeController().lastVersion();
            JFinal.me().getServletContext().setAttribute("lastVersion", response);
            List<Comment> commentList = Comment.dao.findHaveReadIsFalse();
            if (commentList != null && !commentList.isEmpty()) {
                getRequest().setAttribute("noReadComments", commentList);
            }
            getRequest().setAttribute("lastVersion", response);
            getRequest().setAttribute("zrlog", ZrLogConfig.blogProperties);
            getRequest().setAttribute("system", ZrLogConfig.systemProp);

            if (getPara(0) == null || getRequest().getRequestURI().endsWith("admin/") || "login".equals(getPara(0))) {
                redirect(Constants.ADMIN_INDEX);
                return null;
            } else {
                if ("dashboard".equals(getPara(0))) {
                    fillStatistics();
                }
                return "/admin/" + getPara(0);
            }
        } else {
            return "/admin/login";
        }
    }

    private void fillStatistics() {
        getRequest().setAttribute("commCount", Comment.dao.count());
        getRequest().setAttribute("toDayCommCount", Comment.dao.countToDayComment());
        getRequest().setAttribute("clickCount", Log.dao.sumClick());
        getRequest().setAttribute("articleCount", Log.dao.adminCount());
    }

    public String login() {
        if (ZrLogUtil.isPreviewMode()) {
            getRequest().setAttribute("userName", System.getenv("DEFAULT_USERNAME"));
            getRequest().setAttribute("password", System.getenv("DEFAULT_PASSWORD"));
        }
        if (AdminTokenThreadLocal.getUser() != null) {
            redirect(Constants.ADMIN_INDEX);
            return null;
        } else {
            return "/admin/login";
        }
    }

    public void logout() {
        Cookie[] cookies = getRequest().getCookies();
        for (Cookie cookie : cookies) {
            if ("zId".equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setMaxAge(Constants.getSessionTimeout().intValue());
                getResponse().addCookie(cookie);
            }
            if (Constants.ADMIN_TOKEN.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setMaxAge(Constants.getSessionTimeout().intValue());
                cookie.setPath("/");
                adminTokenService.setCookieDomain(getRequest(), cookie);
                getResponse().addCookie(cookie);
            }
        }
        redirect("/admin/login");
    }
}
