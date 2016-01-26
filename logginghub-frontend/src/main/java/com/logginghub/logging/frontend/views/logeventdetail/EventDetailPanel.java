package com.logginghub.logging.frontend.views.logeventdetail;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.ComponentKeys;
import com.logginghub.logging.frontend.Utils;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.EventTableColumnModel;
import com.logginghub.utils.observable.BindableToModel;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EventDetailPanel extends JPanel implements BindableToModel<EnvironmentModel> {

    private final JEditorPane jEditorPane;
    private final JLabel timeColumnLabel;
    private final JLabel levelColumnLabel;
    private final JLabel sourceColumnLabel;
    private final JLabel hostColumnLabel;
    private final JLabel classColumnLabel;
    private final JLabel methodColumnLabel;
    private final JLabel threadColumnLabel;
    private final JLabel loggerColumnLabel;
    private JLabel timestampLabel = new JLabel();
    private JLabel sourceApplicationLabel = new JLabel();
    private JLabel classLabel = new JLabel();
    private JLabel threadLabel = new JLabel();
    private JLabel levelLabel = new JLabel();
    private JLabel sourceHostLabel = new JLabel();
    private JLabel methodLabel = new JLabel();
    private JLabel loggerLabel = new JLabel();

    private JTextArea exceptionArea = new JTextArea();
    private JTextArea messageArea = new JTextArea();

    private MigLayout migLayout;
    private JScrollPane exceptionScroller;
    private JScrollPane messageScroller;
    private EnvironmentModel environmentModel;
    //    private final LevelNamesModel levelNamesModel;
    //    private final EventTableColumnModel eventTableColumnModel;

    public EventDetailPanel( /*LevelNamesModel levelNamesModel, EventTableColumnModel eventTableColumnModel, boolean html*/) {
        //        this.levelNamesModel = levelNamesModel;
        //        this.eventTableColumnModel = eventTableColumnModel;

        setName("eventDetailPanel");

        setBackground(Color.white);
        migLayout = new MigLayout("insets 0, gap 0", "[grow,fill]", "[fill][grow,fill][growprio 10,grow,fill]");
        setLayout(migLayout);

        JPanel topPanel = new JPanel(new MigLayout("insets 1, fill", "[][grow][][grow]", "[][][][]"));

        // TODO : class, method and logger need to be columns really - there is a leaky abstraction on the columns vs the actual event fields.
        timeColumnLabel = new JLabel("Time");
        levelColumnLabel = new JLabel("Level");
        sourceColumnLabel = new JLabel("Source");
        hostColumnLabel = new JLabel("Host");
        classColumnLabel = new JLabel("Class");
        methodColumnLabel = new JLabel("Method");
        threadColumnLabel = new JLabel("Thread");
        loggerColumnLabel = new JLabel("Logger");

        addCopyListener(timestampLabel);
        addCopyListener(sourceApplicationLabel);
        addCopyListener(classLabel);
        addCopyListener(threadLabel);
        addCopyListener(levelLabel);
        addCopyListener(sourceHostLabel);
        addCopyListener(methodLabel);
        addCopyListener(loggerLabel);
        addCopyListener(messageArea);
        addCopyListener(exceptionArea);

        topPanel.add(timeColumnLabel);
        topPanel.add(timestampLabel);
        topPanel.add(levelColumnLabel);
        topPanel.add(levelLabel, "wrap");
        topPanel.add(sourceColumnLabel);
        topPanel.add(sourceApplicationLabel);
        topPanel.add(hostColumnLabel);
        topPanel.add(sourceHostLabel, "wrap");
        topPanel.add(classColumnLabel);
        topPanel.add(classLabel);
        topPanel.add(methodColumnLabel);
        topPanel.add(methodLabel, "wrap");
        topPanel.add(threadColumnLabel);
        topPanel.add(threadLabel);
        topPanel.add(loggerColumnLabel);
        topPanel.add(loggerLabel);

        Font notBold = Font.decode("Arial 10 PLAIN");
        timeColumnLabel.setFont(notBold);
        levelColumnLabel.setFont(notBold);
        sourceColumnLabel.setFont(notBold);
        hostColumnLabel.setFont(notBold);
        classColumnLabel.setFont(notBold);
        methodColumnLabel.setFont(notBold);
        threadColumnLabel.setFont(notBold);
        loggerColumnLabel.setFont(notBold);

        Font bold = Font.decode("Arial 10 BOLD");
        timestampLabel.setFont(bold);
        levelLabel.setFont(bold);
        sourceApplicationLabel.setFont(bold);
        sourceHostLabel.setFont(bold);
        classLabel.setFont(bold);
        methodLabel.setFont(bold);
        threadLabel.setFont(bold);
        loggerLabel.setFont(bold);

        exceptionArea.setLineWrap(true);
        Color exceptionRedColour = Color.decode("#ffdddd");
        exceptionArea.setBackground(exceptionRedColour);
        messageArea.setLineWrap(true);

        topPanel.setBackground(Color.decode("#ddeeff"));

        topPanel.setBorder(BorderFactory.createTitledBorder("Summary"));

        messageArea.setEditable(false);
        messageArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        exceptionArea.setEditable(false);
        exceptionArea.setName("exceptionArea");

        // HTML support
        jEditorPane = new JEditorPane();
        jEditorPane.setEditable(false);
        HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane.setEditorKit(kit);
        Document doc = kit.createDefaultDocument();
        jEditorPane.setDocument(doc);
        jEditorPane.setText("");

        // Default to standard message area
        messageScroller = new JScrollPane(messageArea);

        // Disable auto scroll to caret in the message areas
        DefaultCaret messageCaret = (DefaultCaret) messageArea.getCaret();
        messageCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        messageArea.setName(ComponentKeys.EventDetailMessage.name());

        exceptionScroller = new JScrollPane(exceptionArea);

        // Disable auto scroll to caret in exception areas
        DefaultCaret exceptionCaret = (DefaultCaret) exceptionArea.getCaret();
        exceptionCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        add(topPanel, "cell 0 0");
        add(messageScroller, "cell 0 1");
        add(exceptionScroller, "cell 0 2, hidemode 2");

        exceptionShrunk();


        sourceApplicationLabel.setName(ComponentKeys.EventDetailSourceApplication.name());
    }

    private void addCopyListener(final JLabel label) {
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    StringSelection selection = new StringSelection(label.getText());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                }
            }
        });

    }

    private void addCopyListener(final JTextArea area) {
        area.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    StringSelection selection = new StringSelection(area.getText());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                }
            }
        });

    }

    private void exceptionShrunk() {
        exceptionScroller.setVisible(false);
    }

    @Override
    public void bind(EnvironmentModel environmentModel) {
        this.environmentModel = environmentModel;

        EventTableColumnModel eventTableColumnModel = environmentModel.getEventTableColumnModel();

        timeColumnLabel.setText(eventTableColumnModel.getColumnName(DetailedLogEventTableModel.COLUMN_TIME));
        levelColumnLabel.setText(eventTableColumnModel.getColumnName(DetailedLogEventTableModel.COLUMN_LEVEL));
        sourceColumnLabel.setText(eventTableColumnModel.getColumnName(DetailedLogEventTableModel.COLUMN_SOURCE));
        hostColumnLabel.setText(eventTableColumnModel.getColumnName(DetailedLogEventTableModel.COLUMN_HOST));
        classColumnLabel.setText("Class");
        methodColumnLabel.setText("Method");
        threadColumnLabel.setText(eventTableColumnModel.getColumnName(DetailedLogEventTableModel.COLUMN_THREAD));
        loggerColumnLabel.setText("Logger");

        environmentModel.getShowHTMLEventDetails().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                if(newValue){
                    messageScroller.getViewport().remove(messageArea);
                    messageScroller.getViewport().add(jEditorPane);
                } else {
                    messageScroller.getViewport().add(messageArea);
                    messageScroller.getViewport().remove(jEditorPane);
                }
            }
        });

    }

    @Override
    public void unbind(EnvironmentModel environmentModel) {

    }

    public void clear() {
        messageArea.setText("");
        exceptionArea.setText("");
        timestampLabel.setText("");
        levelLabel.setText("");
        sourceApplicationLabel.setText("");
        sourceHostLabel.setText("");
        classLabel.setText("");
        methodLabel.setText("");
        threadLabel.setText("");
        loggerLabel.setText("");
    }

    public void update(LogEvent event) {

        timestampLabel.setText(Utils.formatTime(event.getOriginTime()));
        levelLabel.setText(environmentModel.getLevelNamesModel().getLevelName(event.getLevel()));
        sourceApplicationLabel.setText(event.getSourceApplication());
        sourceHostLabel.setText(event.getSourceHost() + " (" + event.getSourceAddress() + ")");
        classLabel.setText(event.getSourceClassName());
        methodLabel.setText(event.getSourceMethodName());
        threadLabel.setText(event.getThreadName());
        loggerLabel.setText(event.getLoggerName());

        if (event.getFormattedException() != null) {
            exceptionArea.setText(event.getFormattedException());
            exceptionNormalSize();
        } else {
            exceptionArea.setText("");
            exceptionShrunk();
        }

        if (messageArea != null) {
            messageArea.setText(event.getMessage());
            messageArea.setCaretPosition(0);
            messageArea.getCaret().setMagicCaretPosition(new Point(0, 0));
        }

        if (jEditorPane != null) {
            // TODO : caret stuff?
            jEditorPane.setText(event.getMessage());
        }

        validate();
    }

    private void exceptionNormalSize() {
        exceptionScroller.setVisible(true);
    }
}
