package view;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class EOQSensitivityWindow extends JFrame {

    public EOQSensitivityWindow(double[] valores) {
        setTitle("Analisis de sensibilidad Q*");
        setSize(780, 520);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBackground(AppColors.BG_DARK);
        raiz.setBorder(new EmptyBorder(24, 26, 24, 26));

        JLabel titulo = new JLabel("Sensibilidad de Q* ante cambios en Cp");
        titulo.setForeground(AppColors.TEXT_MAIN);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitulo = new JLabel("Escenario de -50% a +50% en el costo por pedido.");
        subtitulo.setForeground(AppColors.TEXT_MUTED);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titulo, BorderLayout.NORTH);
        header.add(subtitulo, BorderLayout.SOUTH);

        raiz.add(header, BorderLayout.NORTH);
        raiz.add(new SensitivityChart(valores), BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private static class SensitivityChart extends JPanel {
        private final double[] valores;

        SensitivityChart(double[] valores) {
            this.valores = valores;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int left = 70;
            int right = getWidth() - 40;
            int top = 40;
            int bottom = getHeight() - 65;

            g2.setColor(AppColors.CARD_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(AppColors.BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

            double max = 0;
            for (double valor : valores) {
                max = Math.max(max, valor);
            }
            max = Math.max(1, max);

            g2.setColor(new Color(50, 78, 103));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(left, top, left, bottom);
            g2.drawLine(left, bottom, right, bottom);

            g2.setColor(AppColors.GREEN);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int prevX = -1;
            int prevY = -1;
            for (int i = 0; i < valores.length; i++) {
                int x = left + (right - left) * i / (valores.length - 1);
                int y = bottom - (int) ((bottom - top) * valores[i] / max);
                if (prevX >= 0) {
                    g2.drawLine(prevX, prevY, x, y);
                }
                g2.fillOval(x - 4, y - 4, 8, 8);
                prevX = x;
                prevY = y;
            }

            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(AppColors.TEXT_MUTED);
            g2.drawString("-50% Cp", left - 12, bottom + 28);
            g2.drawString("+50% Cp", right - 48, bottom + 28);
            g2.drawString("Q*", 26, top + 8);
            g2.dispose();
        }
    }
}
