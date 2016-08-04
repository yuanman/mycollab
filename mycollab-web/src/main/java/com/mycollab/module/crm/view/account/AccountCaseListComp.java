/**
 * This file is part of mycollab-web.
 *
 * mycollab-web is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-web is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-web.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mycollab.module.crm.view.account;

import com.google.common.base.MoreObjects;
import com.hp.gagawa.java.elements.A;
import com.mycollab.common.i18n.GenericI18Enum;
import com.mycollab.configuration.SiteConfiguration;
import com.mycollab.db.arguments.NumberSearchField;
import com.mycollab.module.crm.CrmDataTypeFactory;
import com.mycollab.module.crm.CrmTypeConstants;
import com.mycollab.module.crm.data.CrmLinkBuilder;
import com.mycollab.module.crm.domain.Account;
import com.mycollab.module.crm.domain.SimpleCase;
import com.mycollab.module.crm.domain.criteria.CaseSearchCriteria;
import com.mycollab.module.crm.i18n.CaseI18nEnum;
import com.mycollab.module.crm.service.CaseService;
import com.mycollab.module.crm.ui.CrmAssetsManager;
import com.mycollab.module.crm.ui.components.RelatedListComp2;
import com.mycollab.module.user.AccountLinkGenerator;
import com.mycollab.security.RolePermissionCollections;
import com.mycollab.spring.AppContextUtil;
import com.mycollab.vaadin.AppContext;
import com.mycollab.vaadin.ui.ELabel;
import com.mycollab.vaadin.web.ui.AbstractBeanBlockList;
import com.mycollab.vaadin.web.ui.ConfirmDialogExt;
import com.mycollab.vaadin.web.ui.WebUIConstants;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.layouts.MHorizontalLayout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author MyCollab Ltd.
 * @since 4.0
 */
public class AccountCaseListComp extends RelatedListComp2<CaseService, CaseSearchCriteria, SimpleCase> {
    private static final long serialVersionUID = -8763667647686473453L;
    private Account account;

    final static Map<String, String> colorsMap;

    static {
        Map<String, String> tmpMap = new HashMap<>();
        for (int i = 0; i < CrmDataTypeFactory.getCasesStatusList().length; i++) {
            String roleKeyName = CrmDataTypeFactory.getCasesStatusList()[i];
            if (!tmpMap.containsKey(roleKeyName)) {
                tmpMap.put(roleKeyName, AbstractBeanBlockList.COLOR_STYLENAME_LIST[i]);
            }
        }
        colorsMap = Collections.unmodifiableMap(tmpMap);
    }

    public AccountCaseListComp() {
        super(AppContextUtil.getSpringBean(CaseService.class), 20);
        this.setBlockDisplayHandler(new AccountCaseBlockDisplay());
    }

    @Override
    protected Component generateTopControls() {
        MHorizontalLayout controlsBtnWrap = new MHorizontalLayout().withFullWidth();

        MHorizontalLayout notesWrap = new MHorizontalLayout().withFullWidth();
        Label noteLbl = new Label("Note: ");
        noteLbl.setSizeUndefined();
        noteLbl.setStyleName("list-note-lbl");
        notesWrap.addComponent(noteLbl);

        CssLayout noteBlock = new CssLayout();
        noteBlock.setWidth("100%");
        noteBlock.setStyleName("list-note-block");
        for (int i = 0; i < CrmDataTypeFactory.getCasesStatusList().length; i++) {
            Label note = new Label(CrmDataTypeFactory.getCasesStatusList()[i]);
            note.setStyleName("note-label");
            note.addStyleName(colorsMap.get(CrmDataTypeFactory.getCasesStatusList()[i]));
            note.setSizeUndefined();
            noteBlock.addComponent(note);
        }
        notesWrap.with(noteBlock).expand(noteBlock);
        controlsBtnWrap.addComponent(notesWrap);

        if (AppContext.canWrite(RolePermissionCollections.CRM_CASE)) {
            MButton createBtn = new MButton(AppContext.getMessage(CaseI18nEnum.NEW), clickEvent -> fireNewRelatedItem(""))
                    .withIcon(FontAwesome.PLUS).withStyleName(WebUIConstants.BUTTON_ACTION);
            controlsBtnWrap.with(createBtn).withAlign(createBtn, Alignment.TOP_RIGHT);
        }

        return controlsBtnWrap;
    }

    public void displayCases(final Account account) {
        this.account = account;
        loadCases();
    }

    private void loadCases() {
        final CaseSearchCriteria criteria = new CaseSearchCriteria();
        criteria.setAccountId(new NumberSearchField(account.getId()));
        setSearchCriteria(criteria);
    }

    @Override
    public void refresh() {
        loadCases();
    }

    public class AccountCaseBlockDisplay implements BlockDisplayHandler<SimpleCase> {

        @Override
        public Component generateBlock(final SimpleCase oneCase, int blockIndex) {
            CssLayout beanBlock = new CssLayout();
            beanBlock.addStyleName("bean-block");
            beanBlock.setWidth("350px");

            VerticalLayout blockContent = new VerticalLayout();
            MHorizontalLayout blockTop = new MHorizontalLayout();
            CssLayout iconWrap = new CssLayout();
            iconWrap.setStyleName("icon-wrap");
            ELabel caseIcon = ELabel.fontIcon(CrmAssetsManager.getAsset(CrmTypeConstants.CASE));
            iconWrap.addComponent(caseIcon);
            blockTop.addComponent(iconWrap);

            VerticalLayout caseInfo = new VerticalLayout();
            caseInfo.setSpacing(true);

            MButton deleteBtn = new MButton("", clickEvent -> {
                ConfirmDialogExt.show(UI.getCurrent(),
                        AppContext.getMessage(GenericI18Enum.DIALOG_DELETE_TITLE, AppContext.getSiteName()),
                        AppContext.getMessage(GenericI18Enum.DIALOG_DELETE_SINGLE_ITEM_MESSAGE),
                        AppContext.getMessage(GenericI18Enum.BUTTON_YES),
                        AppContext.getMessage(GenericI18Enum.BUTTON_NO),
                        confirmDialog -> {
                            if (confirmDialog.isConfirmed()) {
                                CaseService caseService = AppContextUtil.getSpringBean(CaseService.class);
                                caseService.removeWithSession(oneCase, AppContext.getUsername(), AppContext.getAccountId());
                                AccountCaseListComp.this.refresh();
                            }
                        });
            }).withIcon(FontAwesome.TRASH_O).withStyleName(WebUIConstants.BUTTON_ICON_ONLY);

            blockContent.addComponent(deleteBtn);
            blockContent.setComponentAlignment(deleteBtn, Alignment.TOP_RIGHT);

            A caseLink = new A(CrmLinkBuilder.generateCasePreviewLinkFull(oneCase.getId())).appendText(oneCase.getSubject());
            ELabel caseSubject = ELabel.h3(caseLink.write());
            caseInfo.addComponent(caseSubject);

            Label casePriority = new Label(AppContext.getMessage(CaseI18nEnum.FORM_PRIORITY) + ": " + MoreObjects.firstNonNull(oneCase.getPriority(), ""));
            caseInfo.addComponent(casePriority);

            Label caseStatus = new Label(AppContext.getMessage(GenericI18Enum.FORM_STATUS) + ": " + MoreObjects.firstNonNull(oneCase.getStatus(), ""));
            caseInfo.addComponent(caseStatus);

            if (oneCase.getStatus() != null) {
                beanBlock.addStyleName(colorsMap.get(oneCase.getStatus()));
            }

            String assigneeValue = (oneCase.getAssignuser() == null) ? new A().write() : new A(AccountLinkGenerator.generatePreviewFullUserLink(
                    SiteConfiguration.getSiteUrl(AppContext.getUser().getSubdomain()), oneCase.getAssignuser()))
                    .appendText(oneCase.getAssignUserFullName()).write();
            Label caseAssignUser = ELabel.html(assigneeValue);
            caseInfo.addComponent(caseAssignUser);

            ELabel caseCreatedTime = new ELabel(AppContext.getMessage(GenericI18Enum.FORM_CREATED_TIME) + ": "
                    + AppContext.formatPrettyTime(oneCase.getCreatedtime()))
                    .withDescription(AppContext.formatDateTime(oneCase.getCreatedtime()));
            caseInfo.addComponent(caseCreatedTime);

            blockTop.addComponent(caseInfo);
            blockTop.setExpandRatio(caseInfo, 1.0f);
            blockTop.setWidth("100%");
            blockContent.addComponent(blockTop);

            blockContent.setWidth("100%");

            beanBlock.addComponent(blockContent);
            return beanBlock;
        }

    }

}