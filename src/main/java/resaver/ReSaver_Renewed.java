/*
 * Copyright 2016 Mark Fairchild.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package resaver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import resaver.ess.ElementException;
import resaver.gui.Configurator;
import resaver.gui.SaveWindow;

/**
 * Entry class for ReSaver Renewed.
 *
 * @author Mark Fairchild
 */
@Command(name = "ReSaver_Renewed", mixinStandardHelpOptions = true, version = "FallrimTools ReSaver (Renewed) 1.0.0", description = "")
public class ReSaver_Renewed implements Callable<Integer> {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new CommandLine(new ReSaver_Renewed()).execute(args);
    }

    /**
     */
    @Override
    public Integer call() {
        if (CLEAR_OPTION) {
            try {
                PREFS.clear();
            } catch(BackingStoreException ex) {
                LOG.log(Level.WARNING, "Couldn not clear preferences: {0}", ex.getMessage());
            }
            
        }
        
        // Apply look and feel. FlatLaf provides a modern, HiDPI-aware appearance.
        boolean darkMode = DARKTHEME_OPTION || PREFS.getBoolean("settings.darktheme", false);
        if (darkMode) {
            com.formdev.flatlaf.FlatDarkLaf.setup();
        } else {
            com.formdev.flatlaf.FlatLightLaf.setup();
        }

        // Start debug logging if previously enabled.
        if (PREFS.getBoolean("settings.debuglog", false)) {
            resaver.gui.DebugLogger.getInstance().setEnabled(true);
        }

        // Set the font scaling.
        float fontScale = Math.max(0.5f, PREFS.getFloat("settings.fontScale", 1.0f));

        UIManager.getLookAndFeelDefaults().keySet().stream()
                .filter(key -> key.toString().endsWith(".font"))
                .forEach(key -> {
                    java.awt.Font font = UIManager.getFont(key);
                    java.awt.Font biggerFont = font.deriveFont(fontScale * font.getSize2D());
                    UIManager.put(key, biggerFont);
                });

        // Check the autoparse setting.
        Path selection = null;
        
        final Path PREVIOUS = Configurator.getPreviousSave();
        if (PATH_PARAMETER != null && !PATH_PARAMETER.isEmpty() && Configurator.validateSavegame(PATH_PARAMETER.get(0))) {            
            selection = PATH_PARAMETER.get(0);
        } else if (REOPEN_OPTION && Configurator.validateSavegame(PREVIOUS)) {
            selection = PREVIOUS;
        }
        
        if (selection != null && INGR_OPTION) {
            try {
                resaver.ess.ESS.Result result = resaver.ess.ESS.readESS(selection, new resaver.ess.ModelBuilder(new resaver.ProgressModel(1)));
                resaver.ess.ESS save = result.ESS;
                resaver.ess.RefID playerID = save.make(0x400014);
                resaver.ess.ChangeForm form = save.getChangeForms().getChangeForm(playerID);
                resaver.ess.ChangeFormACHR achr = (resaver.ess.ChangeFormACHR) form.getData(null, save.getContext(), true);
                resaver.ess.Element[] inventory = achr.INVENTORY;
                
                for (resaver.ess.Element e : inventory) {
                    resaver.ess.ChangeFormInventoryItem item = (resaver.ess.ChangeFormInventoryItem) e;
                    int count = item.COUNT;
                    resaver.ess.RefID ref = item.ITEM;
                    String plugin = ref.PLUGIN.NAME;
                    int formID = ref.FORMID & (ref.PLUGIN.LIGHTWEIGHT ? 0xFFF : 0xFFFFFF);
                    String formKeyCount = String.format("%06x:%s,%d", formID, plugin, count);
                    System.out.println(formKeyCount);
                }
                
            } catch (IOException | ElementException ex) {
                ex.printStackTrace(System.err);
            }
            return 0;
            
        } else {
            final SaveWindow WINDOW = new SaveWindow(selection, AUTOPARSE_OPTION && selection != null);
            if (WATCH_OPTION) {
                WINDOW.setWatching(true);
            }

            java.awt.EventQueue.invokeLater(() -> WINDOW.setVisible(true));
            return 0;
        }
    }


    static final Logger LOG = Logger.getLogger(ReSaver_Renewed.class.getCanonicalName());
    static final private Preferences PREFS = Preferences.userNodeForPackage(resaver.ReSaver_Renewed.class);

    @Option(names = {"-r", "--reopen"}, description = "Reopens the most recently opened savefile (ignored if a valid savefile is specified).")
    private boolean REOPEN_OPTION;

    @Option(names = {"-p", "--autoparse"}, description = "Automatically scan plugins for the specified savefile (ignored unless a savefile is specified or the -r option is used.")
    private boolean AUTOPARSE_OPTION;

    @Option(names = {"-d", "--darktheme"}, description = "Use the dark theme.")
    private boolean DARKTHEME_OPTION;

    @Option(names = {"-w", "--watch"}, description = "Automatically start watching the savefile directories.")
    private boolean WATCH_OPTION;

    @Option(names = {"-c", "--clear"}, description = "Clear all stored FallrimTools settings.")
    private boolean CLEAR_OPTION;

    @Option(names = {"-i", "--inventory"}, description = "Output player inventory (requires --reopen or a save filename.")
    private boolean INGR_OPTION;

    @Parameters(description = "The savefile to open (optional).")
    private java.util.List<Path> PATH_PARAMETER;

}
