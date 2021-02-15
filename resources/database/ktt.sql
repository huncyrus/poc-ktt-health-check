create table if not exists `ktt_health_check` (
    `id` int(100) not null auto_increment,
    `service_url` varchar(250) not null default '',
    `service_name` varchar(100) not null default '',
    `cr_date` timestamp not null default CURRENT_TIMESTAMP,
    `mod_date` timestamp not null default CURRENT_TIMESTAMP,
    `response_time` int not null default 0 comment 'Service HTTP response time in ms',
    `cr_user` int(100) not null default 0,
    `service_enabled` smallint(1) not null default 1 comment 'for soft-del',
    `service_status` smallint(1) not null default 1 comment 'for later usage',
    primary key(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;
ALTER TABLE `ktt_health_check` ADD INDEX (`service_url`);

create table if not exists `ktt_users` (
    `id` int(100) not null auto_increment,
    `user_name` varchar(100) not null default '',
    `user_pass` varchar(250) not null default '',
    `user_email` varchar(100) not null default '',
    `verified` smallint(1) not null default 0,
    `verify_code` varchar(250) not null default '',
    `last_online` timestamp not null default CURRENT_TIMESTAMP,
    `cr_date` timestamp not null default CURRENT_TIMESTAMP,
    primary key(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;

