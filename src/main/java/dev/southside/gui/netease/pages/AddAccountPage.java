package dev.southside.gui.netease.pages;

import cc.polyfrost.oneconfig.gui.elements.text.TextInputField;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import dev.southside.gui.netease.AbsPage;
import dev.southside.gui.netease.ActionButton;
import dev.southside.gui.netease.NeteaseScreen;
import dev.southside.gui.netease.pages.AccountsPage;
import dev.southside.module.impl.client.Account;
import dev.southside.module.impl.client.Notification;
import dev.southside.nel.NeteaseLauncher;
import dev.southside.utils.network.Http;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AddAccountPage
extends AbsPage {
    private final AbsPage parent;
    public String captchaSession;
    public boolean captchaShow;
    private CompletableFuture<byte[]> captchaDownloader;
    private final ActionButton loginButton = new ActionButton(100, 35, "Add", 2, ColorPalette.PRIMARY);
    private final ActionButton cancelButton = new ActionButton(100, 35, "Cancel", 2, ColorPalette.PRIMARY);
    private final TextInputField account = new TextInputField(300, 50, "Account", false, false);
    private final TextInputField password = new TextInputField(300, 50, "Password", false, false);
    private final TextInputField captcha = new TextInputField(300, 50, "Captcha code", false, false);
    private String captchaFile;

    public AddAccountPage(AbsPage parent, Account.Credentials credentials) {
        this(parent);
        this.account.setInput(credentials.account());
        this.password.setInput(credentials.password());
        this.captcha.setInput("");
        this.loginButton.getRunnable().run();
    }

    public AddAccountPage(AbsPage parent) {
        super("AddAccount");
        this.parent = parent;
        this.loginButton.setClickAction(() -> {
            if (this.account.getInput().isEmpty()) {
                Notification.addNotification("\u8bf7\u8f93\u5165Cookie\u6216\u6709\u6548\u8d26\u53f7", "Error", Notification.NotificationType.DISABLED);
                return;
            }
            this.loginButton.disable(true);
            CompletableFuture future = new CompletableFuture();
            if (this.password.getInput().isEmpty()) {
                NeteaseLauncher.get().makeClient().thenAccept(client -> client.api.loginCookie(this.account.getInput(), 5L, TimeUnit.SECONDS).thenAccept(response -> {
                    future.complete(null);
                    this.loginButton.disable(false);
                    if (response != null) {
                        Notification.addNotification(response, "Login failed", Notification.NotificationType.DISABLED);
                    } else {
                        Notification.addNotification("Login successful", "Added", Notification.NotificationType.ENABLED);
                        if (parent instanceof AccountsPage) {
                            AccountsPage ac = (AccountsPage)parent;
                            ac.refresh().thenRun(() -> NeteaseScreen.INSTANCE.openPage(parent));
                        } else {
                            NeteaseScreen.INSTANCE.openPage(parent);
                        }
                    }
                }));
            } else {
                NeteaseLauncher.get().makeClient().thenAccept(client -> client.api.login4399(this.account.getInput(), this.password.getInput(), this.captchaSession, this.captchaSession != null ? this.captcha.getInput() : null, 5L, TimeUnit.SECONDS).thenAccept(response -> {
                    future.complete(null);
                    this.loginButton.disable(false);
                    if (response != null) {
                        if (response.startsWith("@")) {
                            Notification.addNotification("\u9700\u8981Captcha", "Captcha", Notification.NotificationType.INFO);
                            String session = response.substring(1);
                            this.captchaShow = true;
                            this.captchaSession = session;
                            NeteaseLauncher.get().getExecutor().submit(() -> {
                                this.captchaDownloader = new CompletableFuture();
                                String url = "https://ptlogin.4399.com/ptlogin/captcha.do?captchaId=" + this.captchaSession;
                                try {
                                    byte[] bytes = Http.get(url).sendBytes();
                                    this.captchaDownloader.complete(bytes);
                                }
                                catch (Throwable t) {
                                    Notification.addNotification("captcha\u56fe\u7247\u4e0b\u8f7d\u5931\u8d25: " + t.getMessage(), "ca failed", Notification.NotificationType.DISABLED);
                                    this.captchaDownloader = null;
                                }
                            });
                            NeteaseScreen.INSTANCE.dialog.setTask(this.captchaDownloader, "Downloading captcha..");
                        } else {
                            Notification.addNotification(response, "Login failed", Notification.NotificationType.DISABLED);
                        }
                    } else {
                        Notification.addNotification("Login successful", "Added", Notification.NotificationType.ENABLED);
                        if (parent instanceof AccountsPage) {
                            AccountsPage ac = (AccountsPage)parent;
                            ac.refresh().thenRun(() -> NeteaseScreen.INSTANCE.openPage(parent));
                        } else {
                            NeteaseScreen.INSTANCE.openPage(parent);
                        }
                    }
                }));
            }
            NeteaseScreen.INSTANCE.dialog.setTask(future, "Requesting..");
        });
        this.cancelButton.setClickAction(() -> NeteaseScreen.INSTANCE.openPage(parent));
    }

    @Override
    public void draw(long vg, int x, int y, InputHandler inputHandler) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
        float centerX = (float)x + 528.0f - 70.0f;
        float startY = (float)y + 130.0f;
        nanoVGHelper.drawCenteredText(vg, "Add Account", centerX, startY, -1, 32.0f, Fonts.BOLD);
        float fieldX = centerX - (float)this.account.getWidth() / 2.0f;
        float fieldY = startY + 60.0f;
        this.account.draw(vg, fieldX, fieldY, inputHandler);
        this.password.draw(vg, fieldX, fieldY + (float)this.account.getHeight() + 20.0f, inputHandler);
        float baseY = fieldY + (float)this.account.getHeight() + (float)this.password.getHeight() + 40.0f;
        if (this.captchaShow && this.captchaSession != null) {
            this.captcha.draw(vg, fieldX, fieldY + (float)this.account.getHeight() + 20.0f + (float)this.password.getHeight() + 20.0f, inputHandler);
            baseY += (float)this.captcha.getHeight() + 20.0f;
        }
        float buttonsY = baseY;
        float rightX = centerX + (float)this.account.getWidth() / 2.0f;
        this.cancelButton.draw(vg, rightX - (float)this.cancelButton.getWidth(), buttonsY, inputHandler);
        this.loginButton.draw(vg, rightX - (float)this.cancelButton.getWidth() - (float)this.loginButton.getWidth() - 10.0f, buttonsY, inputHandler);
        if (this.captchaShow && this.captchaSession != null && this.captchaDownloader != null && this.captchaDownloader.isDone()) {
            byte[] bytes = this.captchaDownloader.resultNow();
            try {
                Path file = Files.createTempFile("capt" + this.captchaSession, ".png", new FileAttribute[0]);
                file.toFile().deleteOnExit();
                Files.write(file, bytes, new OpenOption[0]);
                this.captchaFile = file.toAbsolutePath().toString();
            }
            catch (IOException e) {
                Notification.addNotification("\u6587\u4ef6\u52a0\u8f7d\u5931\u8d25", "Temp", Notification.NotificationType.DISABLED);
            }
            nanoVGHelper.drawImage(vg, this.captchaFile, rightX - (float)this.cancelButton.getWidth() - (float)this.loginButton.getWidth() - 10.0f, buttonsY + 50.0f, 100.0f, 100.0f, -1);
        }
    }

    @Override
    public boolean needServiceAvailable() {
        return true;
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        super.keyTyped(keyCode, scanCode, modifiers);
        this.account.keyTyped(keyCode, scanCode, modifiers);
        this.password.keyTyped(keyCode, scanCode, modifiers);
        if (this.captchaShow && this.captchaSession != null) {
            this.captcha.keyTyped(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char c, int modifiers) {
        super.charTyped(c, modifiers);
        this.account.charTyped(c, modifiers);
        this.password.charTyped(c, modifiers);
        if (this.captchaShow && this.captchaSession != null) {
            this.captcha.charTyped(c, modifiers);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.account.mouseReleased(mouseX, mouseY, state);
        this.password.mouseReleased(mouseX, mouseY, state);
        if (this.captchaShow && this.captchaSession != null) {
            this.captcha.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.account.mouseClicked(mouseX, mouseY, mouseButton);
        this.password.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.captchaShow && this.captchaSession != null) {
            this.captcha.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
}

