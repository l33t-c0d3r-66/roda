package org.roda.wui.client.common.slider;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class DisseminationsSliderHelper {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DisseminationsSliderHelper() {

  }

  private static void updateDisseminationsSliderPanel(final IndexedAIP aip,
    final SliderPanel disseminationsSliderPanel) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, aip.getUUID()));
    updateDisseminations(aip, filter, disseminationsSliderPanel);
  }

  private static void updateDisseminationsSliderPanel(IndexedRepresentation representation,
    final SliderPanel disseminationsSliderPanel) {
    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.DIP_REPRESENTATION_UUIDS, representation.getUUID()));
    updateDisseminations(representation, filter, disseminationsSliderPanel);
  }

  private static void updateDisseminationsSliderPanel(IndexedFile file, final SliderPanel disseminationsSliderPanel) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, file.getUUID()));
    updateDisseminations(file, filter, disseminationsSliderPanel);
  }

  private static <T extends IsIndexed> void updateDisseminations(final T object, Filter filter,
    final SliderPanel disseminationsSliderPanel) {
    Sorter sorter = new Sorter(new SortParameter(RodaConstants.DIP_DATE_CREATED, true));
    Sublist sublist = new Sublist(0, 100);
    Facets facets = Facets.NONE;
    String localeString = LocaleInfo.getCurrentLocale().getLocaleName();
    boolean justActive = true;

    BrowserService.Util.getInstance().find(IndexedDIP.class.getName(), filter, sorter, sublist, facets, localeString,
      justActive, new AsyncCallback<IndexResult<IndexedDIP>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(IndexResult<IndexedDIP> result) {
          updateDisseminationsSliderPanel(result.getResults(), object, disseminationsSliderPanel);
        }
      });
  }

  private static <T extends IsIndexed> void updateDisseminationsSliderPanel(List<IndexedDIP> dips, T object,
    SliderPanel disseminationsSliderPanel) {

    disseminationsSliderPanel.clear();
    disseminationsSliderPanel.addTitle(new Label(messages.viewRepresentationFileDisseminationTitle()));

    if (dips.isEmpty()) {
      Label dipEmpty = new Label(messages.browseFileDipEmpty());
      disseminationsSliderPanel.addContent(dipEmpty);
      dipEmpty.addStyleName("dip-empty");
    } else {
      for (final IndexedDIP dip : dips) {
        disseminationsSliderPanel.addContent(createDisseminationPanel(dip, object, disseminationsSliderPanel));
      }
    }
  }

  private static <T extends IsIndexed> FlowPanel createDisseminationPanel(final IndexedDIP dip, final T object,
    final SliderPanel disseminationsSliderPanel) {
    FlowPanel layout = new FlowPanel();

    // open layout
    FlowPanel leftLayout = new FlowPanel();
    Label titleLabel = new Label(dip.getTitle());
    Label descriptionLabel = new Label(dip.getDescription());

    leftLayout.add(titleLabel);
    leftLayout.add(descriptionLabel);

    FocusPanel openFocus = new FocusPanel(leftLayout);
    layout.add(openFocus);

    // delete
    HTML deleteIcon = new HTML(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-ban'></i>"));
    FocusPanel deleteButton = new FocusPanel(deleteIcon);
    deleteButton.addStyleName("lightbtn");
    deleteIcon.addStyleName("lightbtn-icon");
    deleteButton.setTitle(messages.browseFileDipDelete());

    deleteButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        deleteDissemination(dip, object, disseminationsSliderPanel);
      }
    });

    layout.add(deleteButton);

    titleLabel.addStyleName("dipTitle");
    descriptionLabel.addStyleName("dipDescription");
    layout.addStyleName("dip");
    leftLayout.addStyleName("dip-left");
    openFocus.addStyleName("dip-focus");
    deleteButton.addStyleName("dip-delete");

    openFocus.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if (StringUtils.isNotBlank(dip.getOpenExternalURL())) {
          Window.open(dip.getOpenExternalURL(), "_blank", "");
          Toast.showInfo(messages.browseFileDipOpenedExternalURL(), dip.getOpenExternalURL());
        } else {
          HistoryUtils.openBrowse(dip);
        }
      }
    });

    return layout;
  }

  protected static <T extends IsIndexed> void updateDisseminationsObjectSliderPanel(final T object,
    final SliderPanel disseminationsSliderPanel) {
    if (object instanceof IndexedAIP) {
      updateDisseminationsSliderPanel((IndexedAIP) object, disseminationsSliderPanel);
    } else if (object instanceof IndexedRepresentation) {
      updateDisseminationsSliderPanel((IndexedRepresentation) object, disseminationsSliderPanel);
    } else if (object instanceof IndexedFile) {
      updateDisseminationsSliderPanel((IndexedFile) object, disseminationsSliderPanel);
    } else {
      // do nothing
    }
  }

  private static <T extends IsIndexed> void deleteDissemination(final IndexedDIP dip, final T object,
    final SliderPanel disseminationsSliderPanel) {
    Dialogs.showConfirmDialog(messages.browseFileDipRepresentationConfirmTitle(),
      messages.browseFileDipRepresentationConfirmMessage(), messages.dialogCancel(), messages.dialogYes(),
      new AsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            BrowserService.Util.getInstance().deleteDIP(dip.getId(), new AsyncCallback<Void>() {
              @Override
              public void onSuccess(Void result) {
                updateDisseminationsObjectSliderPanel(object, disseminationsSliderPanel);
              }

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }
            });
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }
      });
  }
}