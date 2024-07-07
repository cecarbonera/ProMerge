package com.dvl.promerge.views;
  
import java.awt.BorderLayout; 
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.dvl.core.dao.DaoGenerico;
import com.dvl.core.entitys.ProMergeAvaliacaoComplexidade;
import com.dvl.core.util.AplicationUtils;

import java.util.List;

@SuppressWarnings("serial")
public class ProMergeComplexity extends JFrame {
	// Componentes
	JButton btnLinha = new JButton("[...]");
	JButton btnFechar = new JButton("Close");

	// Actions Listeners
	ActionListener alChamadas, alFecharTela;

	private DefaultTableModel tableDataModel;
	private JTable table;

	public ProMergeComplexity() throws ParseException {
		super("Complexity of Methods/Functions");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		int _width = 800, _height = 600, _heightRodape = 70;

		// Setar o tamanho e visível
		add(criarTabela(_width, _height - _heightRodape), BorderLayout.NORTH);
		add(criarRodape(_width, _heightRodape), BorderLayout.SOUTH);

		// Consultar os registros no banco de dados
		List<ProMergeAvaliacaoComplexidade> lista = new DaoGenerico().buscarListaComplexidades();

		// Foamatar a tabela
		atualizarTabela(lista);

		alChamadas = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				final JFrame frame = new JFrame("Callers...");
				JTextArea ta = new JTextArea(25, 59);

				// Consultar os registros no banco de dados
				String _chamadores = "";
				String _consulta = table.getModel().getValueAt(row, 1).toString();

				List<String> lista = new DaoGenerico().buscarChamadores(_consulta);

				for (String string : lista) {
					_chamadores += string + "\n";

				}

				ta.setText(_chamadores == "" ? "No Callers..." : _chamadores);

				JScrollPane sp = new JScrollPane(ta);
				frame.setLayout(new FlowLayout());

				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setSize(670, 455);
				frame.getContentPane().add(sp);

				frame.setLocationRelativeTo(null);
				frame.setResizable(false);
				frame.setVisible(true);
			}

		};

		alFecharTela = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				setVisible(false);
			}
		};

		// Adicionar os Eventos
		btnLinha.addActionListener(alChamadas);
		btnFechar.addActionListener(alFecharTela);

		setSize(_width, _height);
		setResizable(false);
		setVisible(true);

	}

	/**
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	private Component criarTabela(int width, int height) {
		// Tabela
		JPanel panTabela = new JPanel();

		panTabela.setLayout(new BorderLayout());
		panTabela.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

		String[] colunas = { "Class Name", "Method/Function", "Severity", "References", "Pontuation", " " };

		tableDataModel = new DefaultTableModel();
		tableDataModel.setColumnIdentifiers(colunas);
		table = new JTable(tableDataModel);

		table.setSize(width, height);

		// Definir o tamanho das colunas
		table.getColumnModel().getColumn(0).setPreferredWidth(380);
		table.getColumnModel().getColumn(1).setPreferredWidth(170);
		table.getColumnModel().getColumn(2).setPreferredWidth(75);
		table.getColumnModel().getColumn(3).setPreferredWidth(75);
		table.getColumnModel().getColumn(4).setPreferredWidth(75);
		table.getColumnModel().getColumn(5).setPreferredWidth(20);
		table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
		table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

		// Opções de Ordenação
		table.setRowSorter(new TableRowSorter<TableModel>(tableDataModel));

		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(width, height));

		return panTabela.add(scrollpane);
	}

	/**
	 * 
	 * @param _width
	 * @param height
	 * @return
	 */
	private Component criarRodape(int _width, int height) {
		// Rodapé
		JPanel panelRodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		// Propriedades
		panelRodape.setBounds(0, _width - height, 600, 30);
		panelRodape.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

		//Adicionar o botão fechar
		panelRodape.add(btnFechar);

		// Retornar
		return panelRodape;

	}
	
	/***
	 * 
	 * @param linhas
	 */
	private void atualizarTabela(List<ProMergeAvaliacaoComplexidade> linhas) {
		while (tableDataModel.getRowCount() > 0) {
			tableDataModel.removeRow(0);
		}

		if (linhas == null || linhas.isEmpty()) {
			return;
		}

		String[] linhaDados = new String[6];

		for (int i = 0; i < linhas.size(); i++) {
			// Pegar o registro
			ProMergeAvaliacaoComplexidade _regAtual = linhas.get(i);

			// Atualizar as linhas de dados
			// Classe
			linhaDados[0] = _regAtual.getClasse();
			// Método
			linhaDados[1] = _regAtual.getMetodo();
			// Complexidade
			linhaDados[2] = AplicationUtils.getDescricaoSeveridade(_regAtual.getSeveridade());
			// QtdReferencias
			linhaDados[3] = String.valueOf(_regAtual.getQtdOcorrencias());
			// QtdPontos
			linhaDados[4] = _regAtual.getQtdPontos() == 0 ? "" : String.valueOf(_regAtual.getQtdPontos()).replace(".0", "");

			// Adicionar a linha
			tableDataModel.addRow(linhaDados);

		}

	}

	class ButtonRenderer extends JButton implements TableCellRenderer {
		public ButtonRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setText("[...]");
			return this;

		}

	}

	class ButtonEditor extends DefaultCellEditor {
		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);

		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			btnLinha.setText("[...]");
			return btnLinha;

		}

		public Object getCellEditorValue() {
			return new String("[...]");

		}

	}

}