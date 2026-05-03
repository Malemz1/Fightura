import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

const GITHUB_USER = 'Malemz1';
const REPO_NAME = 'Fightura';

export default defineConfig({
    site: `https://${GITHUB_USER.toLowerCase()}.github.io`,
    base: `/${REPO_NAME}`,
    integrations: [
        starlight({
            title: 'Fightura',
            description: 'Make Figura avatars work with Epic Fight on Forge 1.20.1.',
            defaultLocale: 'root',
            locales: {
                root: { label: 'English', lang: 'en' },
                th: { label: 'ไทย', lang: 'th' },
            },
            social: [
                {
                    icon: 'github',
                    label: 'GitHub',
                    href: `https://github.com/${GITHUB_USER}/${REPO_NAME}`,
                },
            ],
            sidebar: [
                {
                    label: 'Getting Started',
                    translations: { th: 'เริ่มต้น' },
                    items: [
                        {
                            label: 'Installation',
                            translations: { th: 'การติดตั้ง' },
                            link: '/install/',
                        },
                    ],
                },
                {
                    label: 'Reference',
                    translations: { th: 'อ้างอิง' },
                    items: [
                        {
                            label: 'Naming Conventions',
                            translations: { th: 'รูปแบบการตั้งชื่อ' },
                            link: '/naming/',
                        },
                        {
                            label: 'Lua API',
                            translations: { th: 'Lua API' },
                            link: '/lua-api/',
                        },
                    ],
                },
                {
                    label: 'Help',
                    translations: { th: 'ช่วยเหลือ' },
                    items: [
                        {
                            label: 'Troubleshooting',
                            translations: { th: 'แก้ปัญหา' },
                            link: '/troubleshooting/',
                        },
                    ],
                },
            ],
        }),
    ],
});
