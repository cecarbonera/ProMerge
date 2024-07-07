package com.dvl.promerge.views;
 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;

import com.dvl.core.dao.DaoGenerico;
import com.dvl.core.entitys.ProMergeHistoricoCommits;
import java.util.List;
 
@SuppressWarnings("serial")
public class ProMergeCommits extends JFrame {
	// Componentes 
	MaskFormatter _dataFormato = new MaskFormatter("##/##/####");
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	JTextField txtRevisaoI = new JTextField();
	JTextField txtRevisaoF = new JTextField();
	JFormattedTextField txtDataI = new JFormattedTextField(_dataFormato);
	JFormattedTextField txtDataF = new JFormattedTextField(_dataFormato);
	JButton btnPesquisar = new JButton("Search");
	JButton btnLimpar = new JButton("Clean");
	JButton btnLinha = new JButton("[..]");
	JButton btnFechar = new JButton("Close");

	// Actions Listeners
	ActionListener alPesquisar, alLimpar, alHistorico, alFecharTela;

	private DefaultTableModel tableDataModel;
	private JTable table;
	final String _sucesso = "Success";
	final String _erro = "Error";

	public ProMergeCommits() throws ParseException {
		super("Commits History");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		final int _width = 920, _height = 500;

		// Adicionar os componentes
		add(criarHeader(), BorderLayout.NORTH);
		add(criarTabela(_width, _height), BorderLayout.CENTER);
		add(criarRodape(), BorderLayout.SOUTH);

		// Pesquisar dados conforme os parâmetros
		alPesquisar = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int _revisaoI = 0, _revisaoF = 999999;
				if (!txtRevisaoI.getText().isEmpty()) {
					_revisaoI = Integer.valueOf(txtRevisaoI.getText());
				}

				if (!txtRevisaoF.getText().isEmpty()) {
					_revisaoF = Integer.valueOf(txtRevisaoF.getText());
				}

				// Consultar os registros no banco de dados
				List<ProMergeHistoricoCommits> lista = new DaoGenerico().listarHistoricosCommits(_revisaoI, _revisaoF, txtDataI.getText(),
						txtDataF.getText());

				// Foamatar a tabela
				atualizarTabela(lista);

				if (lista == null || lista.isEmpty()) {
					JOptionPane.showMessageDialog(null, "No records found...");
				}

			}

		};

		// Limpar a consulta anterior
		alLimpar = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Limpar os dados antigos
				atualizarTabela(null);
			}
		};

		alHistorico = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				String _mensCompilacao = table.getModel().getValueAt(row, 5).toString();

				final JFrame frame = new JFrame("Evaluation result message...");
				JTextArea ta = new JTextArea(25, 59);
				ta.setText(_mensCompilacao);

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

		// Adicionar o Evento de pesquisar
		btnPesquisar.addActionListener(alPesquisar);
		btnLimpar.addActionListener(alLimpar);
		btnLinha.addActionListener(alHistorico);
		btnFechar.addActionListener(alFecharTela);

		//Setar o tamanho e visível
		setSize(_width, _height);
		setResizable(false);
		setVisible(true);

	}

	/***
	 * 
	 * @return
	 */
	private Component criarHeader() {
		// Painel dos filtros
		JPanel pnlConsulta = new JPanel();
		pnlConsulta.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

		// 1a Coluna
		pnlConsulta.add(new JLabel("Initial Review:"));
		// 2a Coluna
		txtRevisaoI.setColumns(6);

		// Adicionar o componente
		pnlConsulta.add(txtRevisaoI);

		// 3a Coluna
		pnlConsulta.add(new JLabel("Final Review:"));
		// 4a Coluna
		txtRevisaoF.setColumns(6);

		// Adicionar o componente
		pnlConsulta.add(txtRevisaoF);

		// 5a Coluna
		pnlConsulta.add(new JLabel("Initial Date:"));
		

		// 6a Coluna
		txtDataI.setText(dateFormat.format(new Date()));
		txtDataI.setColumns(10);

		// Adicionar o componente
		pnlConsulta.add(txtDataI);

		// 7a Coluna
		pnlConsulta.add(new JLabel("Final Date:"));
		// 8a Coluna
		txtDataF.setText(dateFormat.format(new Date()));
		txtDataF.setColumns(10);

		// Adicionar o componente
		pnlConsulta.add(txtDataF);

		// Sequencia dos botões
		pnlConsulta.add(btnPesquisar);
		pnlConsulta.add(btnLimpar);

		return pnlConsulta;
	}

	/**
	 * 
	 * @return
	 */
	private Component criarRodape() {
		// Painel dos filtros
		JPanel pnlRodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlRodape.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

		// Adicionar o botão fechar
		pnlRodape.add(btnFechar);

		return pnlRodape;
	}

	/***
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

		String[] colunas = { "Review", "User", "Status", "Date/Hour", "Timing Commit", "Message", "Build" };

		tableDataModel = new DefaultTableModel();
		tableDataModel.setColumnIdentifiers(colunas);
		table = new JTable(tableDataModel) {

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
				// Pegar o componente na tela
				Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);

				String status = getValueAt(rowIndex, 2).toString();

				if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
					if (status == _erro) {
						c.setBackground(Color.decode("#FADBD8"));
					} else {
						c.setBackground(Color.decode("#F8F8F8"));
					}

				} else {
					if (status == _erro) {
						c.setBackground(Color.decode("#FADBD8"));
					} else {
						c.setBackground(getBackground());
					}
				}
				return c;
			}

		};

		table.setSize(width, height);

		// Definir o tamanho das colunas
		table.getColumnModel().getColumn(3).setPreferredWidth(120);
		table.getColumnModel().getColumn(4).setPreferredWidth(175);
		table.getColumnModel().getColumn(5).setPreferredWidth(250);
		table.getColumnModel().getColumn(5).setPreferredWidth(250);
		table.getColumnModel().getColumn(6).setPreferredWidth(20);
		table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
		table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

		// Opções de Ordenação
		table.setRowSorter(new TableRowSorter<TableModel>(tableDataModel));
		return panTabela.add(new JScrollPane(table));

	}

	/**
	 * 
	 * @param linhas
	 */
	private void atualizarTabela(List<ProMergeHistoricoCommits> linhas) {
		while (tableDataModel.getRowCount() > 0) {
			tableDataModel.removeRow(0);
		}

		//Se tem dados (retorna o modelo vazio)
		if (linhas == null || linhas.isEmpty()) {
			txtRevisaoI.setText("");
			txtRevisaoF.setText("");
			
			txtDataI.setText(dateFormat.format(new Date()));
			txtDataI.setColumns(10);

			txtDataF.setText(dateFormat.format(new Date()));
			txtDataF.setColumns(10);
			
			return;
		}

		SimpleDateFormat _fmtDataHMS = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String[] linhaDados = new String[8];
		String _tempoCommit = "";

		for (int i = 0; i < linhas.size(); i++) {
			// Pegar o registro
			ProMergeHistoricoCommits _regAtual = linhas.get(i);

			// Se não for a 1a linha
			if (i > 0) {
				ProMergeHistoricoCommits _regAnterior = linhas.get(i - 1);

				// Se o registro atual falhou E o registro anterior foi sucesso e o registro anterior foi a 1inha -> Próximo
				if (_regAtual.getStatus() == 1 && _regAnterior.getStatus() == 0 && (i - 1) == 0)
					// Próximo registro
					_tempoCommit = "";

				else
					// Calcular o intevalo de datas (Anterior - Atual)
					_tempoCommit = "["
							+ _regAnterior.getRevisao()
							+ " -> "
							+ _regAtual.getRevisao()
							+ "] - "
							+ new DaoGenerico().calcularIntervaloEntreDatas(_fmtDataHMS.format(_regAtual.getDtHrCommit()),
									_fmtDataHMS.format(_regAnterior.getDtHrCommit()));
			}

			// Atualizar as linhas de dados
			// Revisao
			linhaDados[0] = String.valueOf(_regAtual.getRevisao());
			// Usuário
			linhaDados[1] = _regAtual.getUsuario();
			// Status
			linhaDados[2] = _regAtual.getStatus() == 0 ? _sucesso : _erro;
			// Data/Hora
			linhaDados[3] = _fmtDataHMS.format(_regAtual.getDtHrCommit());
			// Tempo Commit
			linhaDados[4] = (i == 0) ? "" : _tempoCommit;
			// Mensagem
			linhaDados[5] = _regAtual.getMensagem();

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