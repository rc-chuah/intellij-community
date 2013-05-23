package org.hanuna.gitalk.swing_ui.frame;

import org.hanuna.gitalk.commit.Hash;
import org.hanuna.gitalk.graph.elements.GraphElement;
import org.hanuna.gitalk.graph.elements.Node;
import org.hanuna.gitalk.printmodel.GraphPrintCell;
import org.hanuna.gitalk.printmodel.SpecialPrintElement;
import org.hanuna.gitalk.swing_ui.render.GraphCommitCellRender;
import org.hanuna.gitalk.swing_ui.render.PositionUtil;
import org.hanuna.gitalk.swing_ui.render.painters.GraphCellPainter;
import org.hanuna.gitalk.swing_ui.render.painters.SimpleGraphCellPainter;
import org.hanuna.gitalk.ui.UI_Controller;
import org.hanuna.gitalk.ui.tables.GraphCommitCell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static org.hanuna.gitalk.swing_ui.render.Print_Parameters.EDGE_FIELD;
import static org.hanuna.gitalk.swing_ui.render.Print_Parameters.HEIGHT_CELL;

/**
 * @author erokhins
 */
public class UI_GraphTable extends JTable {
  private final UI_Controller ui_controller;
  private final GraphCellPainter graphPainter = new SimpleGraphCellPainter();
  private final MouseAdapter mouseAdapter = new MyMouseAdapter();

  private Hash myCommitBeingDragged = null;
  private DragDropListener myDragDropListener = new DragDropListener();

  public UI_GraphTable(UI_Controller ui_controller) {
    super(ui_controller.getGraphTableModel());
    UIManager.put("Table.focusCellHighlightBorder", new BorderUIResource(new LineBorder(new Color(255, 0, 0, 0))));
    this.ui_controller = ui_controller;
    prepare();
  }

  private void prepare() {
    setTableHeader(null);
    setDefaultRenderer(GraphCommitCell.class, new GraphCommitCellRender(graphPainter));
    setRowHeight(HEIGHT_CELL);
    setShowHorizontalLines(false);
    setIntercellSpacing(new Dimension(0, 0));

    getColumnModel().getColumn(0).setPreferredWidth(700);
    getColumnModel().getColumn(1).setMinWidth(90);
    getColumnModel().getColumn(2).setMinWidth(90);

    addMouseMotionListener(mouseAdapter);
    addMouseListener(mouseAdapter);
  }

  public void setDragDropListener(@NotNull DragDropListener dragDropListener) {
    myDragDropListener = dragDropListener;
  }

  public void jumpToRow(int rowIndex) {
    scrollRectToVisible(getCellRect(rowIndex, 0, false));
    setRowSelectionInterval(rowIndex, rowIndex);
    scrollRectToVisible(getCellRect(rowIndex, 0, false));
  }

  private class MyMouseAdapter extends MouseAdapter {
    private final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    private final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

    private GraphPrintCell getGraphPrintCell(MouseEvent e) {
      return PositionUtil.getGraphPrintCell(e, getModel());
    }

    private Hash getCommit(MouseEvent e) {
      return PositionUtil.getCommit(e, getModel());
    }

    @Nullable
    private GraphElement overCell(MouseEvent e) {
      int y = PositionUtil.getYInsideRow(e);
      int x = e.getX();
      GraphPrintCell row = getGraphPrintCell(e);
      return graphPainter.mouseOver(row, x, y);
    }

    @Nullable
    private Node arrowToNode(MouseEvent e) {
      int y = PositionUtil.getYInsideRow(e);
      int x = e.getX();
      GraphPrintCell row = getGraphPrintCell(e);
      SpecialPrintElement printElement = graphPainter.mouseOverArrow(row, x, y);
      if (printElement != null) {
        if (printElement.getType() == SpecialPrintElement.Type.DOWN_ARROW) {
          return printElement.getGraphElement().getEdge().getDownNode();
        }
        else {
          return printElement.getGraphElement().getEdge().getUpNode();
        }
      }
      return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 1) {
        Node jumpToNode = arrowToNode(e);
        if (jumpToNode != null) {
          jumpToRow(jumpToNode.getRowIndex());
        }
        GraphElement graphElement = overCell(e);
        ui_controller.click(graphElement);
        if (graphElement == null) {
          ui_controller.click(PositionUtil.getRowIndex(e));
        }
      }
      else {
        int rowIndex = PositionUtil.getRowIndex(e);
        ui_controller.doubleClick(rowIndex);
      }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      Node jumpToNode = arrowToNode(e);
      if (jumpToNode != null) {
        setCursor(HAND_CURSOR);
      }
      else {
        setCursor(DEFAULT_CURSOR);
      }
      ui_controller.over(overCell(e));
    }


    @Override
    public void mousePressed(MouseEvent e) {
      myCommitBeingDragged = getCommit(e);
      if (myCommitBeingDragged != null) {
        myDragDropListener.draggingStarted(myCommitBeingDragged);
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (myCommitBeingDragged == null) return;
      handleEvent(e, myDragDropListener.drop());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      if (myCommitBeingDragged == null) return;
      handleEvent(e, myDragDropListener.drag());
    }

    private void handleEvent(MouseEvent e, DragDropListener.Handler handler) {
      Hash commit = getCommit(e);
      if (commit == null) {
        return;
      }
      int rowIndex = PositionUtil.getRowIndex(e);
      int yOffset = PositionUtil.getYInsideRow(e);

      for (SpecialPrintElement element : getGraphPrintCell(e).getSpecialPrintElements()) {
        if (element.getType() == SpecialPrintElement.Type.COMMIT_NODE) {
          if (PositionUtil.overNode(element.getPosition(), e.getX(), yOffset)) {
            handler.overNode(rowIndex, commit, e, myCommitBeingDragged);
            return;
          }
        }
      }

      if (yOffset <= EDGE_FIELD) {
        handler.above(rowIndex, commit, e, myCommitBeingDragged);
      }
      else if (yOffset >= HEIGHT_CELL - EDGE_FIELD) {
        handler.below(rowIndex, commit, e, myCommitBeingDragged);
      }
      else {
        handler.over(rowIndex, commit, e, myCommitBeingDragged);
      }
    }
  }

}
